create table if not exists worker_log
(
    image_id    uuid        not null,
    request_id  uuid        not null,
    filter_type varchar(32) not null,
    primary key (image_id, request_id, filter_type)
);