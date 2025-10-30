create table hot_keyword
(
    id      bigint auto_increment primary key,
    keyword varchar(255) not null,
    rank_no int          not null
);

create table member
(
    id            bigint auto_increment primary key,
    credit        bigint       not null,
    email         varchar(255) not null,
    nickname      varchar(255) not null,
    password      varchar(255) not null,
    registered_at datetime(6)  not null,
    constraint UKhh9kg6jti4n1eoiertn2k6qsc
        unique (nickname),
    constraint UKmbmcqelty0fbrvxp1q58dn57t
        unique (email)
);

create table notification
(
    id          bigint auto_increment primary key,
    is_read     bit                   not null,
    member_id   bigint                not null,
    message     varchar(255)          not null,
    notified_at datetime(6)           not null,
    title       varchar(255)          not null,
    type        enum ('BID', 'TRADE') null
);

create table purchase_detail
(
    id             bigint auto_increment primary key,
    auction_id     bigint      not null,
    buyer_id       bigint      not null,
    product_id     bigint      not null,
    purchase_at    datetime(6) not null,
    purchase_price bigint      not null
);

create table search_history
(
    id          bigint auto_increment primary key,
    keyword     varchar(255) not null,
    searched_at datetime(6)  not null
);

create table sell_detail
(
    id          bigint auto_increment primary key,
    auction_id  bigint      not null,
    final_price bigint      not null,
    product_id  bigint      not null,
    sell_at     datetime(6) not null,
    seller_id   bigint      not null
);
