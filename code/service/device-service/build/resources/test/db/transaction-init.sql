create table if not exists transaction.client_transaction(
    uid uuid primary key, 
    client_uid uuid not null references core.client(uid),
    transaction_date TIMESTAMP WITH TIME zone not null,
    amount NUMERIC(20, 8) NOT NULL,
    currency_code CHAR(3) NOT NULL,
    description varchar
);

create table if not exists transaction.tariff_transaction(
	uid uuid primary key, 
	tariff_uid uuid not null references core.tariff(uid) on delete cascade,
    transaction_uid uuid not null references transaction.client_transaction(uid) on delete cascade,
    transaction_date TIMESTAMP WITH TIME zone not null,
    expires_at TIMESTAMP WITH TIME zone not null
);

