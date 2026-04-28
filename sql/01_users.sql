create table if not exists Users (
    id serial primary key ,
    name varchar(255),
    password varchar(255)
);