create table if not exists processing.connection_scheme(
	uid uuid primary key, 
	client_uid uuid not null references core.client(uid) on delete cascade,
    scheme_json jsonb not null
);

create table if not exists processing.buffer(
	uid uuid primary key, 
	connection_scheme_uid uuid not null references processing.connection_scheme(uid) on delete cascade
);

create table if not exists processing.buffer_json_datas(
	uid uuid primary key, 
	buffer_uid uuid not null references processing.buffer(uid) on delete cascade,
    data jsonb not null
);
