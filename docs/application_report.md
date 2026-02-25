# RelatÃ³rio Completo da AplicaÃ§Ã£o â€” Frontend

> **Objetivo**: Servir como base para planejamento da API backend.
> A aplicaÃ§Ã£o foi construÃ­da **frontendâ€‘first** â€” todo o estado hoje vive no cliente.

---

## 1. VisÃ£o Geral

**Frontend** Ã© um editor de fluxos visuais (flow/canvas) voltado a construÃ§Ã£o de pipelines de passos de aÃ§Ã£o. O usuÃ¡rio cria **canvases**, cada um contendo um **grafo** de nÃ³s e arestas que representam o fluxo de trabalho.

### Stack TÃ©cnica

| Camada           | Tecnologia                            | VersÃ£o  |
| ---------------- | ------------------------------------- | ------- |
| Bundler          | Vite                                  | 7.3.1   |
| Framework        | React                                 | 19.2.0  |
| Linguagem        | TypeScript                            | 5.9.3   |
| State management | Zustand                               | 5.0.11  |
| Editor de fluxos | @xyflow/react                         | 12.10.1 |
| EstilizaÃ§Ã£o      | TailwindCSS 4 + Vanilla CSS (App.css) | 4.2.1   |
| Componentes UI   | Radix UI (via shadcn)                 | 1.4.3   |
| Roteamento       | react-router-dom                      | 7.13.1  |
| Ãcones           | Lucide React                          | 0.575.0 |
| Testes E2E       | Playwright                            | 1.58.2  |

### Alias de Import

```ts
// vite.config.ts
'@' â†’ './src'
```

---

## 2. Estrutura de Arquivos Completa

```
frontend/
â”œâ”€â”€ src/
â”‚   â”œâ”€â”€ main.tsx                         # Entry point (BrowserRouter + AppRouter)
â”‚   â”œâ”€â”€ App.tsx                          # Layout raiz (<Outlet />)
â”‚   â”œâ”€â”€ App.css                          # Estilos da aplicaÃ§Ã£o (368 linhas)
â”‚   â”œâ”€â”€ AppRouter.tsx                    # DefiniÃ§Ã£o de rotas
â”‚   â”œâ”€â”€ index.css                        # Tema Tailwind + design tokens (195 linhas)
â”‚   â”‚
â”‚   â”œâ”€â”€ lib/
â”‚   â”‚   â””â”€â”€ utils.ts                     # cn() â€” clsx + tailwind-merge
â”‚   â”‚
â”‚   â”œâ”€â”€ components/
â”‚   â”‚   â”œâ”€â”€ nodes/
â”‚   â”‚   â”‚   â””â”€â”€ ActionNode.tsx           # Componente visual do nÃ³
â”‚   â”‚   â””â”€â”€ ui/
â”‚   â”‚       â”œâ”€â”€ button.tsx               # Button (CVA + Radix Slot)
â”‚   â”‚       â””â”€â”€ popover.tsx              # Popover (Radix Popover)
â”‚   â”‚
â”‚   â””â”€â”€ features/
â”‚       â””â”€â”€ flow/
â”‚           â”œâ”€â”€ domain/
â”‚           â”‚   â”œâ”€â”€ flow.types.ts        # Tipos de domÃ­nio
â”‚           â”‚   â””â”€â”€ flow.factory.ts      # FÃ¡bricas de entidades
â”‚           â”œâ”€â”€ store/
â”‚           â”‚   â””â”€â”€ useFlowStore.ts      # Zustand store centralizado
â”‚           â”œâ”€â”€ persistence/
â”‚           â”‚   â””â”€â”€ flowPersistencePolicy.ts  # PolÃ­tica de persistÃªncia
â”‚           â”œâ”€â”€ components/
â”‚           â”‚   â”œâ”€â”€ FlowWorkspace.tsx     # Shell do workspace
â”‚           â”‚   â”œâ”€â”€ FlowToolbar.tsx       # Toolbar de canvases e nodes
â”‚           â”‚   â””â”€â”€ CanvasMartialPattern.tsx  # Background decorativo
â”‚           â””â”€â”€ pages/
â”‚               â”œâ”€â”€ CanvasHomePage.tsx    # PÃ¡gina empty-state / redirect
â”‚               â””â”€â”€ CanvasPage.tsx        # PÃ¡gina principal do canvas
â”‚
â”œâ”€â”€ e2e/
â”‚   â””â”€â”€ canvas-management.spec.ts        # Testes E2E (Playwright)
â”‚
â”œâ”€â”€ package.json
â”œâ”€â”€ vite.config.ts
â”œâ”€â”€ tsconfig.json / tsconfig.app.json / tsconfig.node.json
â”œâ”€â”€ eslint.config.js
â”œâ”€â”€ playwright.config.ts
â””â”€â”€ components.json                      # ConfiguraÃ§Ã£o shadcn
```

---

## 3. Modelo de DomÃ­nio â€” Tipos de Dados

Todos os tipos estÃ£o definidos em [flow.types.ts](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/features/flow/domain/flow.types.ts).

### 3.1 ActionNodeData

Dados de conteÃºdo de um nÃ³ de aÃ§Ã£o.

```ts
type ActionNodeData = {
  title: string; // TÃ­tulo exibido no nÃ³ (ex: "Entrada", "ValidaÃ§Ã£o")
  subtitle: string; // DescriÃ§Ã£o curta (ex: "Coleta de contexto da tarefa")
};
```

### 3.2 ActionFlowNode

NÃ³ tipado do @xyflow/react com tipo `'action'`.

```ts
type ActionFlowNode = Node<ActionNodeData, "action">;
```

> Herda de `Node` do @xyflow: contÃ©m `id: string`, `position: { x, y }`, `type: 'action'`, `data: ActionNodeData`, e opcionais como `selected`, `dragging`, etc.

### 3.3 FlowEdge

Aresta genÃ©rica do @xyflow/react.

```ts
type FlowEdge = Edge;
```

> ContÃ©m `id: string`, `source: string`, `target: string`, e opcionais como `animated: boolean`, `sourceHandle`, `targetHandle`, etc.

### 3.4 FlowGraph

Representa o grafo completo de um canvas.

```ts
type FlowGraph = {
  nodes: ActionFlowNode[]; // Lista de nÃ³s
  edges: FlowEdge[]; // Lista de arestas
};
```

### 3.5 FlowCanvas

**Entidade principal da aplicaÃ§Ã£o.** Representa um canvas com nome, grafo e controle de indexaÃ§Ã£o.

```ts
type FlowCanvas = {
  id: string; // ID Ãºnico (ex: "canvas-a1b2c3d4")
  name: string; // Nome exibido ao usuÃ¡rio
  graph: FlowGraph; // Grafo com nÃ³s e arestas
  nextNodeIndex: number; // PrÃ³ximo Ã­ndice para criaÃ§Ã£o de nÃ³
};
```

### 3.6 FlowCanvasSummary

ProjeÃ§Ã£o leve do canvas para listagens.

```ts
type FlowCanvasSummary = Pick<FlowCanvas, "id" | "name">;
```

### Diagrama de Relacionamento de Entidades

```mermaid
erDiagram
    FlowCanvas {
        string id PK
        string name
        int nextNodeIndex
    }

    FlowGraph {
        ActionFlowNode[] nodes
        FlowEdge[] edges
    }

    ActionFlowNode {
        string id PK
        string type
        float positionX
        float positionY
    }

    ActionNodeData {
        string title
        string subtitle
    }

    FlowEdge {
        string id PK
        string source FK
        string target FK
        boolean animated
    }

    FlowCanvas ||--|| FlowGraph : "graph"
    FlowGraph ||--|{ ActionFlowNode : "nodes"
    FlowGraph ||--|{ FlowEdge : "edges"
    ActionFlowNode ||--|| ActionNodeData : "data"
    FlowEdge }o--|| ActionFlowNode : "source"
    FlowEdge }o--|| ActionFlowNode : "target"
```

---

## 4. FÃ¡bricas de Entidades (Factory)

Todas as funÃ§Ãµes de criaÃ§Ã£o estÃ£o em [flow.factory.ts](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/features/flow/domain/flow.factory.ts).

### 4.1 createCanvasId()

Gera um ID Ãºnico usando `crypto.randomUUID()`.

```ts
// Retorno: "canvas-a1b2c3d4"
function createCanvasId(): string;
```

### 4.2 createCanvasModel(input)

Cria um `FlowCanvas` completo com grafo inicial.

```ts
function createCanvasModel(input: { id: string; name: string }): FlowCanvas;
```

O grafo inicial (`INITIAL_FLOW`) contÃ©m **3 nÃ³s** e **2 arestas**:

| NÃ³ ID      | TÃ­tulo    | SubtÃ­tulo                      | PosiÃ§Ã£o    |
| ---------- | --------- | ------------------------------ | ---------- |
| `start`    | Entrada   | Coleta de contexto da tarefa   | (80, 120)  |
| `validate` | ValidaÃ§Ã£o | Checagem de regras e estrutura | (380, 120) |
| `done`     | SaÃ­da     | Retorno final da execuÃ§Ã£o      | (680, 120) |

| Aresta ID        | Source     | Target     |
| ---------------- | ---------- | ---------- |
| `start-validate` | `start`    | `validate` |
| `validate-done`  | `validate` | `done`     |

O `nextNodeIndex` Ã© inicializado como `graph.nodes.length + 1` (ou seja, `4`).

### 4.3 createActionNode(nodeIndex)

Cria um novo nÃ³ de aÃ§Ã£o com posiÃ§Ã£o calculada por grid.

```ts
function createActionNode(nodeIndex: number): ActionFlowNode;
```

**Layout em grid:**

```ts
const GRID_LAYOUT = {
  columns: 3, // 3 colunas
  startX: 80, // offset X inicial
  startY: 280, // offset Y inicial (abaixo dos nÃ³s iniciais)
  stepX: 300, // espaÃ§amento horizontal
  stepY: 150, // espaÃ§amento vertical
};
```

- ID gerado: `step-{nodeIndex}` (ex: `step-4`)
- TÃ­tulo padrÃ£o: `Passo {nodeIndex}`
- SubtÃ­tulo padrÃ£o: `"Novo bloco para seu fluxo"`

### 4.4 createLinkedEdge(sourceId, targetId)

Cria uma aresta animada entre dois nÃ³s.

```ts
function createLinkedEdge(sourceId: string, targetId: string): FlowEdge;
// { id: "sourceId-targetId", source: sourceId, target: targetId, animated: true }
```

---

## 5. Store â€” Zustand (Estado Global)

O store Ã© definido em [useFlowStore.ts](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/features/flow/store/useFlowStore.ts).

### 5.1 Estado

```ts
type FlowState = {
  canvases: FlowCanvas[]; // Array de todos os canvases
  persistenceMode: PersistenceMode; // 'guest-local' | 'authenticated-api'
};
```

### 5.2 AÃ§Ãµes (OperaÃ§Ãµes)

Abaixo, cada aÃ§Ã£o do store documentada em detalhe, pronta para ser mapeada a endpoints de API:

---

#### `createCanvas(name: string): string`

**OperaÃ§Ã£o**: CriaÃ§Ã£o de canvas  
**ParÃ¢metros**: `name` â€” nome do canvas (string)  
**LÃ³gica**:

1. Gera um novo `canvasId` via `createCanvasId()`
2. Se o nome estiver vazio/whitespace, usa fallback `"Canvas {n+1}"`
3. Cria o canvas com `createCanvasModel({ id, name })`
4. Adiciona ao final do array `canvases`
5. Retorna o `canvasId` criado

**Dados retornados**: `string` (canvasId)

> [!IMPORTANT]
> **Equivalente API**: `POST /canvases` com body `{ name }` â†’ retorna `{ id, name, graph, nextNodeIndex }`

---

#### `renameCanvas(canvasId: string, nextName: string): void`

**OperaÃ§Ã£o**: RenomeaÃ§Ã£o de canvas  
**ParÃ¢metros**: `canvasId`, `nextName`  
**LÃ³gica**:

1. Localiza o canvas pelo `canvasId`
2. Normaliza o nome (trim); se vazio, mantÃ©m o nome anterior
3. Atualiza o campo `name`

> [!IMPORTANT]
> **Equivalente API**: `PATCH /canvases/:canvasId` com body `{ name }`

---

#### `deleteCanvas(canvasId: string, activeCanvasId: string | null): string | null`

**OperaÃ§Ã£o**: ExclusÃ£o de canvas  
**ParÃ¢metros**: `canvasId` (canvas a excluir), `activeCanvasId` (canvas ativo atualmente na view)  
**LÃ³gica**:

1. Localiza o Ã­ndice do canvas pelo `canvasId`
2. Se nÃ£o encontrado, retorna sem mudanÃ§a
3. Filtra o canvas removido da lista
4. Se a lista ficou vazia, retorna `null` (sem canvas ativo)
5. Se o canvas excluÃ­do era o ativo, calcula um fallback:
   - Tenta o canvas no mesmo Ã­ndice â†’ senÃ£o, o anterior â†’ senÃ£o, o primeiro
6. Retorna o `nextActiveCanvasId`

> [!IMPORTANT]
> **Equivalente API**: `DELETE /canvases/:canvasId`
> O roteamento para o prÃ³ximo canvas Ã© responsabilidade do frontend.

---

#### `addActionNodeToCanvas(canvasId: string): void`

**OperaÃ§Ã£o**: AdiÃ§Ã£o de novo nÃ³ de aÃ§Ã£o a um canvas  
**ParÃ¢metros**: `canvasId`  
**LÃ³gica**:

1. Localiza o canvas pelo `canvasId`
2. Cria novo nÃ³ com `createActionNode(canvas.nextNodeIndex)`
3. Se houver nÃ³s existentes, cria uma aresta do **Ãºltimo nÃ³** para o **novo nÃ³**
4. Adiciona o nÃ³ e a aresta ao grafo
5. Incrementa `nextNodeIndex`

> [!IMPORTANT]
> **Equivalente API**: `POST /canvases/:canvasId/nodes` â†’ retorna `{ node, edge? }`

---

#### `connectNodesByCanvas(canvasId: string, connection: Connection): void`

**OperaÃ§Ã£o**: ConexÃ£o manual entre dois nÃ³s (drag de handle)  
**ParÃ¢metros**: `canvasId`, `connection` (contÃ©m `source` e `target`)  
**LÃ³gica**:

1. Valida que `source` e `target` existem
2. Adiciona a aresta ao grafo via `addEdge()` do @xyflow, com `animated: true`

> [!IMPORTANT]
> **Equivalente API**: `POST /canvases/:canvasId/edges` com body `{ source, target }`

---

#### `handleNodesChangeByCanvas(canvasId: string, changes: NodeChange[]): void`

**OperaÃ§Ã£o**: AtualizaÃ§Ã£o batch de nÃ³s (posiÃ§Ã£o, seleÃ§Ã£o, remoÃ§Ã£o)  
**ParÃ¢metros**: `canvasId`, `changes` (array de `NodeChange` do @xyflow)  
**LÃ³gica**: Aplica as mudanÃ§as via `applyNodeChanges()` do @xyflow.

Tipos de `NodeChange` possÃ­veis (definidos pelo @xyflow):

- `NodePositionChange` â€” usuÃ¡rio arrastou o nÃ³
- `NodeSelectionChange` â€” seleÃ§Ã£o mudou
- `NodeRemoveChange` â€” nÃ³ deletado (via tecla Delete)
- `NodeDimensionChange` â€” dimensÃµes mudaram
- `NodeAddChange` â€” nÃ³ adicionado (nÃ£o usado diretamente aqui)

> [!IMPORTANT]
> **Equivalente API**: `PATCH /canvases/:canvasId/nodes` com batch de alteraÃ§Ãµes.
> Considerar debounce para mudanÃ§as de posiÃ§Ã£o para nÃ£o sobrecarregar a API.

---

#### `handleEdgesChangeByCanvas(canvasId: string, changes: EdgeChange[]): void`

**OperaÃ§Ã£o**: AtualizaÃ§Ã£o batch de arestas (remoÃ§Ã£o, seleÃ§Ã£o)  
**ParÃ¢metros**: `canvasId`, `changes` (array de `EdgeChange` do @xyflow)  
**LÃ³gica**: Aplica as mudanÃ§as via `applyEdgeChanges()` do @xyflow.

> [!IMPORTANT]
> **Equivalente API**: `PATCH /canvases/:canvasId/edges` ou `DELETE /canvases/:canvasId/edges/:edgeId`

---

#### `hasCanvas(canvasId: string): boolean`

**OperaÃ§Ã£o**: VerificaÃ§Ã£o de existÃªncia (somente leitura, usada internamente)  
**NÃ£o precisa de endpoint direto**, pois Ã© derivado do listagem.

---

### 5.3 Resumo de OperaÃ§Ãµes (Mapa para API)

| OperaÃ§Ã£o Frontend                | MÃ©todo HTTP Sugerido | Endpoint Sugerido     | Body / Params        |
| -------------------------------- | -------------------- | --------------------- | -------------------- |
| `createCanvas`                   | `POST`               | `/canvases`           | `{ name }`           |
| `renameCanvas`                   | `PATCH`              | `/canvases/:id`       | `{ name }`           |
| `deleteCanvas`                   | `DELETE`             | `/canvases/:id`       | â€”                    |
| `addActionNodeToCanvas`          | `POST`               | `/canvases/:id/nodes` | â€”                    |
| `connectNodesByCanvas`           | `POST`               | `/canvases/:id/edges` | `{ source, target }` |
| `handleNodesChangeByCanvas`      | `PATCH`              | `/canvases/:id/nodes` | `{ changes: [...] }` |
| `handleEdgesChangeByCanvas`      | `PATCH` / `DELETE`   | `/canvases/:id/edges` | `{ changes: [...] }` |
| Listar canvases (implÃ­cito)      | `GET`                | `/canvases`           | â€”                    |
| Buscar canvas por ID (implÃ­cito) | `GET`                | `/canvases/:id`       | â€”                    |

---

## 6. PersistÃªncia

Definida em [flowPersistencePolicy.ts](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/features/flow/persistence/flowPersistencePolicy.ts).

### 6.1 Modos de PersistÃªncia

```ts
type PersistenceMode = "guest-local" | "authenticated-api";
```

| Modo                | Storage                       | DescriÃ§Ã£o                                               |
| ------------------- | ----------------------------- | ------------------------------------------------------- |
| `guest-local`       | `localStorage`                | Dados salvos no navegador do usuÃ¡rio (sem autenticaÃ§Ã£o) |
| `authenticated-api` | `noopStorage` _(placeholder)_ | Preparado para futura integraÃ§Ã£o com API (noop hoje)    |

### 6.2 Chave de Storage

```ts
FLOW_PERSIST_STORAGE_KEY = "flow-canvases-v1";
```

### 6.3 Dados Persistidos

O Zustand `persist` middleware serializa **apenas o array `canvases`**:

```ts
partialize: (state) => ({
  canvases: state.canvases,
});
```

### 6.4 Formato no localStorage

```json
{
  "state": {
    "canvases": [
      {
        "id": "canvas-a1b2c3d4",
        "name": "Meu Canvas",
        "graph": {
          "nodes": [
            {
              "id": "start",
              "type": "action",
              "data": {
                "title": "Entrada",
                "subtitle": "Coleta de contexto da tarefa"
              },
              "position": { "x": 80, "y": 120 }
            }
          ],
          "edges": [
            { "id": "start-validate", "source": "start", "target": "validate" }
          ]
        },
        "nextNodeIndex": 4
      }
    ]
  },
  "version": 1
}
```

### 6.5 DecisÃ£o de Modo

```ts
function getAuthSnapshot(): AuthSnapshot {
  return { isAuthenticated: false }; // TODO(auth): leitura real
}
```

Atualmente **sempre retorna `guest-local`**. A funÃ§Ã£o `resolvePersistenceMode()` estÃ¡ preparada para ler o estado de autenticaÃ§Ã£o futuro e trocar para `authenticated-api`.

> [!WARNING]
> Quando a API existir, o modo `authenticated-api` deve substituir o `localStorage` pelo fetch/mutation Ã  API. O `noopStorage` atual garante que nenhum dado Ã© escrito em local quando autenticado â€” os dados virÃ£o do servidor.

---

## 7. Roteamento

Definido em [AppRouter.tsx](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/AppRouter.tsx).

```mermaid
flowchart TD
    ROOT["/ (RootRedirect)"] --> |"canvases.length > 0"| CANVAS_PAGE["/canvas/:canvasId (CanvasPage)"]
    ROOT --> |"canvases.length == 0"| CANVAS_HOME["/canvas (CanvasHomePage)"]
    CANVAS_HOME --> |"canvas criado"| CANVAS_PAGE
    WILDCARD["/* (catch-all)"] --> ROOT
```

| Rota                | Componente       | DescriÃ§Ã£o                                                 |
| ------------------- | ---------------- | --------------------------------------------------------- |
| `/`                 | `RootRedirect`   | Redirect para primeiro canvas ou `/canvas`                |
| `/canvas`           | `CanvasHomePage` | Empty state â€” instrui o usuÃ¡rio a criar o primeiro canvas |
| `/canvas/:canvasId` | `CanvasPage`     | Renderiza o editor de fluxo do canvas ativo               |
| `/*`                | `RootRedirect`   | Catch-all para rotas inexistentes                         |

### LÃ³gica de Redirecionamento

- **CanvasHomePage**: Se jÃ¡ existe pelo menos um canvas, redireciona automaticamente para ele via `<Navigate>`.
- **CanvasPage**: Se o `canvasId` da URL nÃ£o existe, redireciona para o primeiro canvas disponÃ­vel. Se nÃ£o existe nenhum canvas, redireciona para `/canvas`.

---

## 8. Componentes â€” Ãrvore Completa

### 8.1 Hierarquia

```mermaid
graph TD
    A["main.tsx"] --> B["BrowserRouter"]
    B --> C["AppRouter"]
    C --> D["App (Outlet)"]
    D --> E["CanvasHomePage | CanvasPage"]
    E --> F["FlowWorkspace"]
    F --> G["FlowToolbar"]
    F --> H["children (ReactFlow ou Empty State)"]
    H --> I["CanvasMartialPattern"]
    H --> J["Background"]
    H --> K["Controls"]
    H --> L["ActionNode (por nÃ³)"]
```

### 8.2 ActionNode

Arquivo: [ActionNode.tsx](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/components/nodes/ActionNode.tsx)

Componente visual do nÃ³ de aÃ§Ã£o no editor de fluxos.

- **Props**: `data: ActionNodeData`, `selected: boolean`
- **Handles**:
  - `target` na posiÃ§Ã£o `Left` (entrada)
  - `source` na posiÃ§Ã£o `Right` (saÃ­da)
- **CSS**: `.action-node`, `.action-node--selected`

### 8.3 FlowWorkspace

Arquivo: [FlowWorkspace.tsx](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/features/flow/components/FlowWorkspace.tsx)

Shell do workspace que conecta o store Ã  toolbar e ao conteÃºdo.

- **Props**: `activeCanvasId?`, `children`, `nodeActionsDisabled?`, `onAddNode`
- **Responsabilidades**:
  - Extrai `canvasSummaries` do store
  - Conecta `createCanvas`, `renameCanvas`, `deleteCanvas` do store ao `FlowToolbar`
  - Gerencia navegaÃ§Ã£o entre canvases

### 8.4 FlowToolbar

Arquivo: [FlowToolbar.tsx](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/features/flow/components/FlowToolbar.tsx)

Barra de ferramentas fixa no canto superior esquerdo.

**Dois popovers:**

1. **Popover de Canvases** (Ã­cone `FolderKanban`):
   - Input para criar novo canvas
   - Lista de canvases existentes
   - Para cada canvas: abrir, renomear (inline editing), excluir
   - Badge "Ativo" no canvas ativo
   - ValidaÃ§Ãµes: botÃ£o "Criar" desabilitado se nome vazio

2. **Popover de Nodes** (Ã­cone `Plus`):
   - BotÃ£o "Adicionar novo nÃ³" (desabilitado se sem canvas ativo)

**Estado local do componente:**

- `isCanvasPopoverOpen: boolean`
- `newCanvasName: string`
- `isCreatingCanvas: boolean`
- `editingCanvasId: string | null`
- `editingCanvasName: string`

### 8.5 CanvasPage

Arquivo: [CanvasPage.tsx](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/features/flow/pages/CanvasPage.tsx)

PÃ¡gina principal renderizando o `ReactFlow` com:

- `nodeTypes` customizado (`action â†’ ActionNode`)
- `fitView` habilitado
- `Background` com gap 20 e size 1.2
- `Controls` do @xyflow
- `CanvasMartialPattern` como background decorativo

**Callbacks**: `onConnect`, `onNodesChange`, `onEdgesChange`, `onAddNode` â€” todos delegam para o store com o `canvasId` da URL.

### 8.6 CanvasMartialPattern

Arquivo: [CanvasMartialPattern.tsx](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/features/flow/components/CanvasMartialPattern.tsx)

Background decorativo com grid de Ã­cones de artes marciais (Swords, HandFist, Target, Shield, Flame). Calcula quantidade de Ã­cones baseado no tamanho da viewport. Responsivo ao resize.

---

## 9. Design System

### 9.1 Tema

Definido em [index.css](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/src/index.css) com variÃ¡veis CSS usando **oklch**.

- **Dois temas**: Light (`:root`) e Dark (`.dark`)
- **Tipografia**: `ui-monospace, 'JetBrains Mono', 'Fira Code', 'Menlo', monospace`
- **Border radius**: `0rem` (estilo brutalist/angular)
- **Sombras**: Offset fixo (2px light, 3px dark) sem blur â€” estilo neo-brutalist

### 9.2 Paleta de Cores Principais

| Token           | Papel                          | Hue (approx)    |
| --------------- | ------------------------------ | --------------- |
| `--primary`     | AÃ§Ãµes primÃ¡rias, handles, ring | Verde (~153Â°)   |
| `--secondary`   | AÃ§Ãµes secundÃ¡rias              | Laranja (~61Â°)  |
| `--destructive` | AÃ§Ãµes destrutivas              | Vermelho (~22Â°) |
| `--muted`       | Backgrounds sutis              | Verde claro     |
| `--accent`      | Hovers e destaques             | Verde claro     |

### 9.3 Componentes UI (shadcn)

#### Button (button.tsx)

Variantes de `variant`:

- `default`, `destructive`, `outline`, `secondary`, `ghost`, `link`

Variantes de `size`:

- `default`, `xs`, `sm`, `lg`, `icon`, `icon-xs`, `icon-sm`, `icon-lg`

#### Popover (popover.tsx)

Sub-componentes: `Popover`, `PopoverTrigger`, `PopoverContent`, `PopoverAnchor`, `PopoverHeader`, `PopoverTitle`, `PopoverDescription`

---

## 10. Cobertura de Testes E2E

Arquivo: [canvas-management.spec.ts](file:///c:/Users/juan/Desktop/idkwhyyyy/frontend/e2e/canvas-management.spec.ts)

| Teste                            | O que valida                                                                 |
| -------------------------------- | ---------------------------------------------------------------------------- |
| Empty state e validaÃ§Ã£o de botÃ£o | Mostra mensagem vazia, botÃ£o "Criar" desabilitado com input vazio/whitespace |
| CriaÃ§Ã£o e navegaÃ§Ã£o              | Cria canvases, navega entre eles, verifica URLs e badge "Ativo"              |
| RenomeaÃ§Ã£o e exclusÃ£o            | EdiÃ§Ã£o inline, exclusÃ£o com fallback de rota para outro canvas               |
| PersistÃªncia (guest)             | Dados sobrevivem ao reload via localStorage                                  |

---

## 11. Fluxo de Dados Completo

```mermaid
sequenceDiagram
    actor User
    participant UI as FlowToolbar / CanvasPage
    participant Store as useFlowStore (Zustand)
    participant Factory as flow.factory
    participant Persist as Zustand Persist Middleware
    participant Storage as localStorage

    Note over User,Storage: CriaÃ§Ã£o de Canvas
    User->>UI: Digita nome + clica "Criar"
    UI->>Store: createCanvas("Meu Canvas")
    Store->>Factory: createCanvasId()
    Factory-->>Store: "canvas-abc123"
    Store->>Factory: createCanvasModel({ id, name })
    Factory-->>Store: FlowCanvas com grafo inicial
    Store->>Persist: set({ canvases: [...canvases, newCanvas] })
    Persist->>Storage: setItem("flow-canvases-v1", JSON)
    Store-->>UI: canvasId
    UI->>UI: navigate(/canvas/canvas-abc123)

    Note over User,Storage: AdiÃ§Ã£o de NÃ³
    User->>UI: Clica "Adicionar novo nÃ³"
    UI->>Store: addActionNodeToCanvas("canvas-abc123")
    Store->>Factory: createActionNode(4)
    Factory-->>Store: ActionFlowNode
    Store->>Factory: createLinkedEdge(lastNodeId, newNodeId)
    Factory-->>Store: FlowEdge
    Store->>Persist: set({ canvases: updated })
    Persist->>Storage: setItem("flow-canvases-v1", JSON)

    Note over User,Storage: ConexÃ£o Manual
    User->>UI: Drag handle source â†’ target
    UI->>Store: connectNodesByCanvas("canvas-abc123", connection)
    Store->>Persist: set({ canvases: updated })
    Persist->>Storage: setItem("flow-canvases-v1", JSON)

    Note over User,Storage: MovimentaÃ§Ã£o de NÃ³
    User->>UI: Drag nÃ³ para nova posiÃ§Ã£o
    UI->>Store: handleNodesChangeByCanvas("canvas-abc123", positionChanges)
    Store->>Persist: set({ canvases: updated })
    Persist->>Storage: setItem("flow-canvases-v1", JSON)
```

---

## 12. Pontos Relevantes para Planejamento da API

### 12.1 O que a API precisa cobrir

1. **AutenticaÃ§Ã£o de usuÃ¡rios** â€” hoje nÃ£o existe, mas o `PersistenceMode` jÃ¡ prevÃª `'authenticated-api'`
2. **CRUD completo de Canvases** â€” criar, listar, buscar por ID, renomear, excluir
3. **CRUD de NÃ³s** dentro de um canvas â€” criar, atualizar posiÃ§Ã£o, remover
4. **CRUD de Arestas** dentro de um canvas â€” criar, remover
5. **PersistÃªncia do grafo** â€” nÃ³s com posiÃ§Ã£o (x, y), dados (title, subtitle), e arestas com source/target

### 12.2 Campos que a API precisa gerenciar

| Entidade   | Campos                                                          | Notas                                          |
| ---------- | --------------------------------------------------------------- | ---------------------------------------------- |
| **User**   | id, email, password, ...                                        | NÃ£o existe no frontend hoje                    |
| **Canvas** | id, name, nextNodeIndex, userId (FK), createdAt, updatedAt      | `nextNodeIndex` pode ser gerenciado no backend |
| **Node**   | id, canvasId (FK), type, title, subtitle, positionX, positionY  | type sempre `'action'` hoje                    |
| **Edge**   | id, canvasId (FK), source (FKâ†’Node), target (FKâ†’Node), animated | animated sempre `true` hoje                    |

### 12.3 ConsideraÃ§Ãµes de Design

> [!WARNING]
> **Debounce de posiÃ§Ã£o**: Quando o usuÃ¡rio arrasta nÃ³s, `handleNodesChangeByCanvas` Ã© chamado em alta frequÃªncia. A API deve considerar um mecanismo de debounce/throttle no frontend ou batch update no backend para evitar excesso de requests.

> [!NOTE]
> **`nextNodeIndex`**: Este campo Ã© usado para gerar IDs Ãºnicos e posiÃ§Ãµes em grid. No backend, pode ser substituÃ­do por auto-increment ou UUID + cÃ¡lculo de posiÃ§Ã£o server-side.

> [!NOTE]
> **Grafo inicial**: Ao criar um canvas, 3 nÃ³s e 2 arestas sÃ£o criados automaticamente. Essa lÃ³gica pode ser replicada no backend ou mantida no frontend fazendo uma chamada `POST /canvases` seguida de `POST /canvases/:id/nodes` para cada nÃ³.

> [!TIP]
> **Alternativa mais simples**: Em vez de endpoints granulares de nÃ³s e arestas, a API pode aceitar um `PATCH /canvases/:id` com o grafo inteiro (nÃ³s + arestas), salvando tudo de uma vez. Isso simplifica a integraÃ§Ã£o em troca de maior payload.
