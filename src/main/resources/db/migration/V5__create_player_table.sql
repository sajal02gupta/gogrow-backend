create table if not exists core.player (
    player_id uuid primary key,
    date_of_birth date not null,
    user_id uuid not null references core.users(id),
    deleted_at timestamp with time zone,
    name varchar(255),
    gender varchar(16) check (gender in ('MALE', 'FEMALE', 'OTHER')),
    profile_image_url varchar(1024)
    created_at timestamp with time zone not null,
    modified_at timestamp with time zone
);

create index if not exists idx_player_user_id on core.player(user_id);
