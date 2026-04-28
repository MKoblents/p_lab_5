create table if not exists Chapters (
    id serial primary key ,
    name varchar(255),
    parent_legion varchar(255),
    world varchar(255)
)