drop table if exists auth_session;
drop table if exists app_user;

create table if not exists users (
    id uuid primary key,
    name varchar(255),
    email varchar(255),
    phone varchar(20) not null unique,
    created_at timestamp not null,
    modified_at timestamp not null,
    deleted_at timestamp
);

create table if not exists auth_session (
    id bigserial primary key,
    user_id uuid not null references users(id),
    token_hash varchar(64) not null unique,
    created_at timestamp not null,
    last_active_at timestamp not null,
    revoked_at timestamp
);
