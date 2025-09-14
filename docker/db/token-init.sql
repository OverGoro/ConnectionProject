create  table if not exists access.refresh_token (
	uid uuid primary key,
	client_id uuid not null references core.client(uid) on delete cascade,
	token varchar(512) not null unique,
	
	created_at TIMESTAMP WITH TIME zone not null,
    expires_at TIMESTAMP WITH TIME zone not null,
    
    CONSTRAINT chk_refresh_token_expiry CHECK (expires_at > created_at)
);

create table if not exists access.device_token(
	uid uuid primary key, 
	device_uid uuid not null references core.device(uid) on delete cascade,
	token varchar(512) not null unique,	
	created_at TIMESTAMP WITH TIME zone not null,
    expires_at TIMESTAMP WITH TIME zone not null
);

create table if not exists access.device_refresh_token(
	uid uuid primary key,
	device_token_uid uuid not null references access.device_token(uid) on delete cascade,
	token varchar(512) not null unique,	
	created_at TIMESTAMP WITH TIME zone not null,
    expires_at TIMESTAMP WITH TIME zone not null
);