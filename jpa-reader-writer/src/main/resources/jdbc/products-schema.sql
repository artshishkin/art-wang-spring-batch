create table if not exists categories
(
    id          int          not null auto_increment,
    name        varchar(50)  not null,
    description varchar(100) not null,
    primary key (id)
);

create table if not exists products_jpa
(
    prod_id     int            not null auto_increment,
    prod_name   varchar(50)    not null,
    prod_desc   varchar(100)   not null,
    price       decimal(19, 2) not null,
    unit        long           not null,
    category_id int            not null,
    primary key (prod_id),
    foreign key (category_id) references categories (id)
);

create table if not exists products_jpa_out
(
    id          int            not null auto_increment,
    name        varchar(50)    not null,
    description varchar(100)   not null,
    price       decimal(19, 2) not null,
    unit        long           not null,
    category_id int            not null,
    primary key (id),
    foreign key (category_id) references categories (id)
);

create table if not exists reviews
(
    id         int          not null auto_increment,
    author     varchar(50)  not null,
    content    varchar(100) not null,
    product_id int          default null,
    primary key (id),
    foreign key (product_id) references products_jpa_out (id)
);