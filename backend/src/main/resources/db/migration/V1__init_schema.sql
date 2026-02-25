create table if not exists users (
  id uuid primary key,
  email varchar(190) not null unique,
  password_hash varchar(255),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

insert into users (id, email, password_hash, created_at, updated_at)
values (
  '00000000-0000-0000-0000-000000000001',
  'system@local',
  null,
  now(),
  now()
)
on conflict (id) do nothing;

create table if not exists canvases (
  id uuid primary key,
  user_id uuid not null references users(id) on delete cascade,
  name varchar(120) not null,
  next_node_index integer not null default 1,
  version bigint not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_canvases_user_id on canvases(user_id);

create table if not exists canvas_nodes (
  id uuid primary key,
  canvas_id uuid not null references canvases(id) on delete cascade,
  node_key varchar(80) not null,
  type varchar(40) not null,
  title varchar(120) not null,
  subtitle varchar(255) not null,
  position_x double precision not null,
  position_y double precision not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint uq_canvas_nodes_canvas_node_key unique (canvas_id, node_key)
);

create index if not exists idx_canvas_nodes_canvas_id on canvas_nodes(canvas_id);

create table if not exists canvas_edges (
  id uuid primary key,
  canvas_id uuid not null references canvases(id) on delete cascade,
  edge_key varchar(120) not null,
  source_key varchar(80) not null,
  target_key varchar(80) not null,
  animated boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint uq_canvas_edges_canvas_edge_key unique (canvas_id, edge_key)
);

create index if not exists idx_canvas_edges_canvas_id on canvas_edges(canvas_id);
create index if not exists idx_canvas_edges_source_target on canvas_edges(canvas_id, source_key, target_key);

