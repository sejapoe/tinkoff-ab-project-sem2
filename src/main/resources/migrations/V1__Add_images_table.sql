create table if not exists images
(
    id       uuid         not null primary key,
    filename varchar(255) not null,
    size     bigint       not null
);