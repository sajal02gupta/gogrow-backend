create table if not exists app_user (
    id bigserial primary key,
    phone_number varchar(20) not null unique,
    created_at timestamp not null
);

create table if not exists otp_challenge (
    id bigserial primary key,
    phone_number varchar(20) not null,
    otp_hash varchar(64) not null,
    expires_at timestamp not null,
    consumed_at timestamp,
    attempts integer not null default 0,
    created_at timestamp not null
);

create index if not exists idx_otp_challenge_phone_number on otp_challenge(phone_number);

create table if not exists auth_session (
    id bigserial primary key,
    user_id bigint not null references app_user(id),
    token_hash varchar(64) not null unique,
    created_at timestamp not null,
    last_active_at timestamp not null,
    revoked_at timestamp
);
