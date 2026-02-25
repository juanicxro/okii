# Backend API Plan (Spring Boot)

## 1) Objetivo
Criar uma API Java Spring Boot para substituir o estado local atual do frontend (Zustand + localStorage), cobrindo:
- CRUD de canvases
- Operacoes de nodes e edges por canvas
- Persistencia em banco relacional
- Base pronta para autenticacao por usuario

Este plano foi alinhado com o relatorio atual do frontend (`output/doc/frontend_relatorio_completo_2026-02-25.docx`), especialmente a secao "Mapeamento para API backend".

## 2) Escopo inicial (MVP)
Incluir no MVP:
- Endpoints de canvases e grafo
- Persistencia PostgreSQL
- Validacoes de entrada
- Tratamento de erros padronizado
- Migracoes de schema com Flyway
- Documentacao OpenAPI
- Suite de testes (unitario + integracao com Testcontainers)

Fora do MVP (fase seguinte):
- Login completo (JWT issuer externo ou auth server proprio)
- Rate limit
- Multi-tenant avancado
- Auditoria detalhada por evento

## 3) Stack recomendada
- Java: 21 (LTS)
- Build: Maven
- Framework: Spring Boot (linha estavel atual do projeto)
- Banco: PostgreSQL

### Dependencias Spring Boot
Core:
- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-actuator`

Seguranca (habilitar ja no projeto, mesmo que com regra permissiva no MVP):
- `spring-boot-starter-security`

Banco e migracao:
- `org.postgresql:postgresql`
- `org.flywaydb:flyway-core`

API docs:
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`

Produtividade (opcional):
- `org.projectlombok:lombok`

Testes:
- `spring-boot-starter-test`
- `org.testcontainers:junit-jupiter`
- `org.testcontainers:postgresql`
- `spring-security-test`

## 4) Bootstrap do projeto
Gerar projeto com:
- Group: `com.idkwhyyyy`
- Artifact: `backend-api`
- Package base: `com.idkwhyyyy.backend`
- Java: 21
- Build tool: Maven

Estrutura recomendada:
- `com.idkwhyyyy.backend.common` (errors, problem details, utils)
- `com.idkwhyyyy.backend.config` (security, jackson, openapi)
- `com.idkwhyyyy.backend.canvas` (controller/service/repository/dto)
- `com.idkwhyyyy.backend.graph` (nodes/edges domain + handlers)
- `com.idkwhyyyy.backend.persistence` (entities, mappers, flyway support)
- `com.idkwhyyyy.backend.auth` (principal resolver, ownership)

## 5) Modelo de dados (primeira versao)

### Tabelas
`users`:
- `id` (UUID PK)
- `email` (unique)
- `password_hash` (ou subject externo)
- `created_at`, `updated_at`

`canvases`:
- `id` (UUID PK)
- `user_id` (FK -> users.id)
- `name` (varchar 120)
- `next_node_index` (int not null default 1)
- `version` (bigint, optimistic lock)
- `created_at`, `updated_at`

`canvas_nodes`:
- `id` (UUID PK tecnico)
- `canvas_id` (FK -> canvases.id)
- `node_key` (varchar 80)  // corresponde ao `node.id` do frontend (ex: start, step-4)
- `type` (varchar 40)      // hoje: action
- `title` (varchar 120)
- `subtitle` (varchar 255)
- `position_x` (double precision)
- `position_y` (double precision)
- `created_at`, `updated_at`
- unique `(canvas_id, node_key)`

`canvas_edges`:
- `id` (UUID PK tecnico)
- `canvas_id` (FK -> canvases.id)
- `edge_key` (varchar 120) // corresponde ao `edge.id` do frontend
- `source_key` (varchar 80)
- `target_key` (varchar 80)
- `animated` (boolean not null default true)
- `created_at`, `updated_at`
- unique `(canvas_id, edge_key)`

Indices:
- `canvases(user_id)`
- `canvas_nodes(canvas_id)`
- `canvas_edges(canvas_id)`
- `canvas_edges(canvas_id, source_key, target_key)`

## 6) Contrato de API (v1)
Prefixo: `/api/v1`

### Canvas
- `POST /canvases`
  - body: `{ "name": "Meu Canvas" }`
  - resposta: canvas completo + graph inicial

- `GET /canvases`
  - resposta: lista de summaries (`id`, `name`, `updatedAt`)

- `GET /canvases/{canvasId}`
  - resposta: canvas completo com `graph.nodes` e `graph.edges`

- `PATCH /canvases/{canvasId}`
  - body: `{ "name": "Novo nome" }`
  - resposta: canvas atualizado

- `DELETE /canvases/{canvasId}`
  - resposta: `204 No Content`

### Nodes / Edges (granular, alinhado ao frontend atual)
- `POST /canvases/{canvasId}/nodes`
  - body opcional: `{ "title": "...", "subtitle": "...", "position": { "x": 0, "y": 0 } }`
  - resposta: node criado (+ edge criada automaticamente quando aplicavel)

- `PATCH /canvases/{canvasId}/nodes`
  - body: `{ "changes": [...] }` (modelo inspirado em NodeChange do frontend)
  - resposta: grafo atualizado

- `POST /canvases/{canvasId}/edges`
  - body: `{ "source": "nodeA", "target": "nodeB" }`
  - resposta: edge criada

- `PATCH /canvases/{canvasId}/edges`
  - body: `{ "changes": [...] }`
  - resposta: grafo atualizado

### Opcao alternativa (fase 2, se simplificar cliente)
- `PUT /canvases/{canvasId}/graph`
  - body: graph inteiro
  - resposta: graph persistido

## 7) Regras de negocio principais
- Nome de canvas:
  - trim
  - obrigatorio
  - max 120 chars
- `nextNodeIndex`:
  - mantido no backend
  - incremento atomico ao criar node automatica
- Delete de canvas:
  - backend remove entidade
  - fallback de rota continua no frontend (ja implementado)
- Integridade de edge:
  - `source_key` e `target_key` devem existir no mesmo canvas
  - nao permitir self-loop no MVP (opcional, decidir)

## 8) Erros e padrao de resposta
Usar `application/problem+json` (RFC 7807) com:
- `type`
- `title`
- `status`
- `detail`
- `instance`
- `traceId` (quando existir)

Codigos:
- `400` validacao
- `401` sem autenticacao
- `403` sem permissao
- `404` recurso nao encontrado
- `409` conflito de versao/duplicidade
- `422` regra de negocio invalida

## 9) Seguranca (planejamento)
MVP:
- Security habilitado com policy simples para dev (`permitAll`) ou user mockado por header.

Fase 2:
- JWT bearer token
- ownership por `user_id` em todos os acessos de canvas
- filtros por usuario no repository

## 10) Observabilidade
- Actuator:
  - `/actuator/health`
  - `/actuator/info`
  - `/actuator/metrics`
- Logging estruturado com correlation id (`traceId`)
- Auditoria minima:
  - create/update/delete de canvas
  - create/delete de node/edge

## 11) Test strategy
Unitarios:
- services de canvas e graph (regras de negocio)
- mapeadores DTO <-> domain

Integracao:
- repository + migracoes Flyway (Testcontainers PostgreSQL)
- API endpoints principais com MockMvc/WebTestClient

Contrato:
- snapshots OpenAPI
- testes de serializacao para payloads de graph

## 12) Roadmap de execucao

### Fase 0 - Setup (0.5 a 1 dia)
- gerar projeto
- configurar profiles (`local`, `test`)
- configurar Docker Compose para PostgreSQL local
- configurar Flyway baseline

### Fase 1 - Canvas CRUD (1 a 2 dias)
- entidades + repositorios + services
- endpoints de canvas
- validacoes e tratamento de erro
- testes unitarios e integracao

### Fase 2 - Graph API (2 a 3 dias)
- tabelas de nodes/edges
- endpoints nodes/edges (POST/PATCH)
- regras de integridade source/target
- testes completos de grafo

### Fase 3 - Hardening (1 a 2 dias)
- OpenAPI refinado
- observabilidade minima
- controles de concorrencia (`@Version`)
- documentacao final de contrato

### Fase 4 - Auth (2+ dias)
- JWT
- ownership real
- ajustes no frontend para modo `authenticated-api`

## 13) Definicoes pendentes (decidir antes da implementacao)
- Estrategia de auth no MVP:
  - A) sem auth (somente dev)
  - B) mock de usuario por header
  - C) JWT real desde o inicio
- Persistencia de graph:
  - A) somente granular (`/nodes`, `/edges`)
  - B) adicionar endpoint de graph inteiro (`PUT /graph`)
- Convencao de IDs de node/edge:
  - manter string semantic (`step-4`, `start-validate`) ou UUID tecnico + campo externo

## 14) Criterios de pronto (Definition of Done)
- `mvn test` verde
- migracoes sobem do zero sem ajuste manual
- OpenAPI acessivel e consistente
- endpoints mapeados no frontend documentados
- erros padronizados em `problem+json`
- README backend com setup local e comandos

