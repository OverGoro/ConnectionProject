create table if not exists processing.connection_scheme(
uid uuid primary key, 
client_uid uuid not null references core.client(uid) on delete cascade,
scheme_json jsonb not null
);

create table if not exists processing.buffer(
uid uuid primary key, 
connection_scheme_uid uuid not null references processing.connection_scheme(uid) on delete cascade,
max_messages_number integer not null CHECK (max_messages_number > 0),
max_message_size integer not null CHECK ( max_message_size > 0 ),
message_prototype varchar not null
);

create table if not exists processing.buffer_devices(
buffer_uid uuid not null references processing.buffer(uid) on delete cascade,
device_uid uuid not null references core.device(uid) on delete cascade,
constraint pk_buffer_devices primary key (buffer_uid, device_uid)
);

create table if not exists processing.buffer_json_datas(
uid uuid primary key, 
buffer_uid uuid not null references processing.buffer(uid) on delete cascade,
data jsonb not null, 
created_at TIMESTAMP WITH TIME zone not null
);
