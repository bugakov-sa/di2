create table metric_code (
    code integer primary key,
    description text not null
);

create table record_code (
    code integer primary key,
    description text not null
);

create table record(
    id bigint primary key,
    time bigint not null,
    text text not null,
    code integer not null
);

create table metric(
    code integer not null,
    time bigint not null,
    value text not null,
    record_id bigint,
    primary key(code, time)
);