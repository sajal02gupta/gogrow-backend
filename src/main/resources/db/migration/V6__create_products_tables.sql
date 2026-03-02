create table if not exists core.products (
    id uuid primary key,
    name varchar(255) not null,
    category varchar(255),
    description text,
    is_rentable boolean not null default false,
    product_image text,
    product_dimension text,
    created_at timestamp with time zone not null,
    modified_at timestamp with time zone
);

create table if not exists core.product_prices (
    id uuid primary key,
    product_id uuid not null references core.products(id) on delete cascade,
    price_type varchar(16) not null check (price_type in ('ONE_TIME', 'RENTAL')),
    amount numeric(12,2) not null,
    billing_cycle varchar(16) check (billing_cycle in ('WEEKLY', 'MONTHLY', 'QUARTERLY')),
    security_deposit_amount numeric(12,2),
    is_active boolean not null,
    created_at timestamp with time zone not null,
    modified_at timestamp with time zone
);

create index if not exists idx_product_prices_product_id on core.product_prices(product_id);
