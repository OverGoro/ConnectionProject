create table if not exists core.client(
	uid uuid primary key,
	email varchar not null unique,
	birth_date date,
	username varchar not null unique
);