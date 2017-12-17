create table records (
    user_id bigint not null,
    time bigint not null,
    text varchar(10000) not null,
    primary key(user_id, time)
);