CREATE USER guest WITH ENCRYPTED PASSWORD 'guest';

CREATE DATABASE wallet_service OWNER guest;

create table accounts
(
    id           bigserial
        primary key,
    account_name varchar(100) not null,
    description  text
);

alter table accounts
    owner to guest;

-- auto-generated definition
create table transfers
(
    id                 bigserial
        primary key,
    from_account       integer        not null
        constraint fk_from_account_id
            references accounts,
    to_account         integer        not null
        constraint fk_to_account_id
            references accounts,
    created_date       timestamp      not null,
    amount             numeric(15, 6) not null,
    status             varchar(10)    not null,
    last_modified_date timestamp,
    description        text,
    transaction_number varchar(100),
    transaction_type   varchar(100),
    currency           varchar(20),
    transaction_id     varchar(50),
    related_txn_id     varchar(30)
);

alter table transfers
    owner to guest;

