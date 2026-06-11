create table if not exists Users (
    id serial primary key ,
    name varchar(255) unique,
    password varchar(255),
    parent int,
    foreign key (parent) references Users(id)
);