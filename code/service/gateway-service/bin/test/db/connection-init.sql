create table if not exists processing.connection_scheme(
    uid uuid primary key, 
    client_uid uuid not null references core.client(uid) on delete cascade,
    scheme_json jsonb not null
);


create table if not exists processing.buffer(
    uid uuid primary key, 
    device_uid uuid not null references core.device(uid) on delete cascade,
    max_gateways_number integer not null CHECK (max_gateways_number > 0),
    max_gateway_size integer not null CHECK ( max_gateway_size > 0 ),
    gateway_prototype varchar
);


create table if not exists processing.connection_scheme_buffer(
    uid uuid primary key,
    scheme_uid uuid not null references processing.connection_scheme(uid) on delete cascade,
    buffer_uid uuid not null references processing.buffer(uid) on delete cascade
);

create table if not exists processing.gateway(
    uid uuid primary key, 
    buffer_uid uuid not null references processing.buffer(uid) on delete cascade,
    content jsonb not null, 
    content_type varchar not null, -- incomming/outcomming
    created_at TIMESTAMP WITH TIME zone not null
);


