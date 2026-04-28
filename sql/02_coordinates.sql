create table if not exists Coordinates (
    id serial primary key ,
    x bigint check ( x>-617 ) default 0,
    y bigint check ( y>-842 ) default 0

)