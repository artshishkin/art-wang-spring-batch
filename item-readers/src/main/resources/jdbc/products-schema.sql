create table if not exists products
(
    prod_id   long         not null,
    prod_name varchar(50)  not null,
    prod_desc varchar(100) not null,
    price     double       not null,
    unit      long         not null
);