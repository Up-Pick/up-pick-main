create table auction
(
    id             bigint auto_increment primary key,
    current_price  bigint                                      null,
    end_at         datetime(6)                                 not null,
    last_bidder_id bigint                                      null,
    min_price      bigint                                      not null,
    product_id     bigint                                      not null,
    register_id    bigint                                      not null,
    start_at       datetime(6)                                 not null,
    status         enum ('EXPIRED', 'FINISHED', 'IN_PROGRESS') not null
);

create table bidding_detail
(
    id         bigint auto_increment primary key,
    auction_id bigint      not null,
    bid_at     datetime(6) not null,
    bid_price  bigint      not null,
    bidder_id  bigint      not null
);

create table category
(
    id         bigint auto_increment primary key,
    big        varchar(255) not null,
    created_at datetime(6)  not null,
    small      varchar(255) not null
);

create table product
(
    id             bigint auto_increment primary key,
    big_category   varchar(255) not null,
    category_id    bigint       not null,
    description    varchar(255) not null,
    image          varchar(255) null,
    name           varchar(255) not null,
    register_id    bigint       not null,
    registered_at  datetime(6)  not null,
    small_category varchar(255) not null,
    view_count     bigint       null
);