create  table if not exists refresh_token (
	uid uuid primary key,
	client_id uuid not null references core.client(uid) on delete cascade,
	token varchar(512) not null unique,
	family_id uuid not null unique,
	
	created_at TIMESTAMP WITH TIME zone not null,
    expires_at TIMESTAMP WITH TIME zone not null,
    
    -- constraints
    CONSTRAINT chk_refresh_token_expiry CHECK (expires_at > created_at)
);