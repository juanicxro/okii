param(
  [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

$envFile = Join-Path $PSScriptRoot "..\.env"
if (-not (Test-Path $envFile)) {
  throw ".env nao encontrado. Copie .env.example para .env e ajuste as credenciais."
}

Get-Content $envFile | ForEach-Object {
  $line = $_.Trim()
  if ($line -eq "" -or $line.StartsWith("#")) {
    return
  }

  $name, $value = $line -split "=", 2
  if ($name -and $value -ne $null) {
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), "Process")
  }
}

Write-Host "Running API on port $Port with DB_URL=$env:DB_URL"
& "$PSScriptRoot\..\mvnw.cmd" spring-boot:run "-Dspring-boot.run.arguments=--server.port=$Port"
if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}
