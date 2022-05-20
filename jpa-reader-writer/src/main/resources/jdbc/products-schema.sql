create table if not exists products_jpa
(
    prod_id   int         not null auto_increment,
    prod_name varchar(50)  not null,
    prod_desc varchar(100) not null,
    price     double       not null,
    unit      long         not null,
    primary key (prod_id)
);