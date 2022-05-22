insert into categories (name, description)
values ('Hardware', 'All hardware');
insert into categories (name, description)
values ('Software', 'All software');

insert into products_jpa (prod_name, prod_desc, price, unit, category_id)
values ('Apple JPA', 'apple cell phone', 500.00, 10, 1);
insert into products_jpa (prod_name, prod_desc, price, unit, category_id)
values ('Dell JPA', 'dell computer', 3000, 5, 1);
insert into products_jpa (prod_name, prod_desc, price, unit, category_id)
values ('office JPA', 'ms office software', 196, 23, 2);