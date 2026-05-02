create table if not exists Space_marines (
    id serial primary key ,
    name varchar(255),
    coordinates int,
    creation_date timestamp default now(),
    health  float check ( health>=0 ) default 0,
    astartes_category astartes_category,
    weapon weapon,
    melee_weapon melee_weapon,
    chapter int,
    owner int not null,
    foreign key (owner) references Users(id),
    foreign key (chapter) references Chapters(id),
    foreign key (coordinates) references Coordinates(id)
);