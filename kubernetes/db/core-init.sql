create table if not exists core.client(
	uid uuid primary key,
	email varchar not null unique,
	birth_date date,
	username varchar not null unique,
	password varchar not null
);

create table if not exists core.device(
	uid uuid primary key, 
	client_uuid uuid not null references core.client(uid),
	device_name varchar(100) not null, 
	device_description varchar(500) not null
);

create table if not exists core.tariff(
	uid uuid primary key, 
	tariff_name varchar(100) not null, 
	amount NUMERIC(20, 8) NOT NULL,
    currency_code CHAR(3) NOT NULL
);
