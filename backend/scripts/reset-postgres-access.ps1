param(
  [string]$ServiceName = "postgresql-x64-18",
  [string]$PgRoot = "C:\\Program Files\\PostgreSQL\\18",
  [string]$DbName = "okimanager",
  [string]$DbUser = "postgres",
  [string]$NewPassword = "OkiManager@2026!",
  [string]$Host = "127.0.0.1",
  [int]$Port = 5432
)

$ErrorActionPreference = "Stop"

function Assert-Admin {
  $identity = [Security.Principal.WindowsIdentity]::GetCurrent()
  $principal = New-Object Security.Principal.WindowsPrincipal($identity)
  if (-not $principal.IsInRole([Security.Principal.WindowsBuiltinRole]::Administrator)) {
    throw "Execute este script em PowerShell como Administrador."
  }
}

function Update-HbaAuthMethod {
  param(
    [string]$Path,
    [string]$Method
  )

  $hba = Get-Content $Path
  $hba = $hba -replace "^(local\\s+all\\s+all\\s+)\\S+", "`$1$Method"
  $hba = $hba -replace "^(host\\s+all\\s+all\\s+127\\.0\\.0\\.1/32\\s+)\\S+", "`$1$Method"
  $hba = $hba -replace "^(host\\s+all\\s+all\\s+::1/128\\s+)\\S+", "`$1$Method"
  Set-Content -Path $Path -Value $hba -Encoding ascii
}

function Invoke-Psql {
  param(
    [string]$PsqlExe,
    [string]$Host,
    [int]$Port,
    [string]$User,
    [string]$Database,
    [string]$Sql
  )

  $args = @(
    "-h", $Host,
    "-p", "$Port",
    "-U", $User,
    "-d", $Database,
    "-w",
    "-v", "ON_ERROR_STOP=1",
    "-c", $Sql
  )

  $output = & $PsqlExe @args 2>&1
  if ($LASTEXITCODE -ne 0) {
    $text = ($output | Out-String).Trim()
    throw "psql falhou (exit $LASTEXITCODE): $text"
  }

  return $output
}

Assert-Admin

$pgData = Join-Path $PgRoot "data"
$pgHba = Join-Path $pgData "pg_hba.conf"
$psql = Join-Path $PgRoot "bin\\psql.exe"
$envFile = Join-Path $PSScriptRoot "..\\.env"

if (-not (Test-Path $pgHba)) { throw "Arquivo nao encontrado: $pgHba" }
if (-not (Test-Path $psql)) { throw "Arquivo nao encontrado: $psql" }

$backup = Join-Path $pgData ("pg_hba.conf.bak-" + (Get-Date -Format "yyyyMMdd-HHmmss"))
Copy-Item $pgHba $backup -Force
Write-Host "Backup criado: $backup"

try {
  Update-HbaAuthMethod -Path $pgHba -Method "trust"
  Restart-Service $ServiceName
  Write-Host "Auth local temporario: trust"

  Invoke-Psql -PsqlExe $psql -Host $Host -Port $Port -User $DbUser -Database "postgres" -Sql "SELECT 'trust-ok';" | Out-Host

  $escapedPassword = $NewPassword.Replace("'", "''")
  Invoke-Psql -PsqlExe $psql -Host $Host -Port $Port -User $DbUser -Database "postgres" -Sql "ALTER USER $DbUser WITH PASSWORD '$escapedPassword';" | Out-Host
  Write-Host "Senha do usuario '$DbUser' atualizada."

  Update-HbaAuthMethod -Path $pgHba -Method "scram-sha-256"
  Restart-Service $ServiceName
  Write-Host "Auth local restaurado para scram-sha-256"

  $env:PGPASSWORD = $NewPassword
  Invoke-Psql -PsqlExe $psql -Host $Host -Port $Port -User $DbUser -Database "postgres" -Sql "SELECT current_user, current_database();" | Out-Host

  $exists = & $psql -h $Host -p $Port -U $DbUser -d postgres -t -A -w -c "SELECT 1 FROM pg_database WHERE datname='$DbName';" 2>&1
  if ($LASTEXITCODE -ne 0) {
    $text = ($exists | Out-String).Trim()
    throw "Falha ao verificar banco '$DbName': $text"
  }

  if (($exists | Out-String).Trim() -ne "1") {
    Invoke-Psql -PsqlExe $psql -Host $Host -Port $Port -User $DbUser -Database "postgres" -Sql "CREATE DATABASE $DbName;" | Out-Host
    Write-Host "Banco '$DbName' criado."
  } else {
    Write-Host "Banco '$DbName' ja existe."
  }

  @"
DB_URL=jdbc:postgresql://localhost:5432/$DbName?connectTimeout=10&sslmode=prefer
DB_USER=$DbUser
DB_PASSWORD=$NewPassword
DB_POOL_SIZE=10

POSTGRES_DB=$DbName
POSTGRES_USER=$DbUser
POSTGRES_PASSWORD=$NewPassword
POSTGRES_PORT=5432
"@ | Set-Content $envFile -Encoding ascii

  Write-Host ".env atualizado em: $envFile"
  Write-Host "Concluido."
} catch {
  Write-Error $_
  throw
}
