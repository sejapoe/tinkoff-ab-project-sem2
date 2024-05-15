create table if not exists image_requests
(
    id                uuid        not null primary key        default gen_random_uuid(),
    original_image_id uuid        not null references images (id),
    edited_image_id   uuid        null references images (id) default null,
    status            varchar(32) not null                    default 'WIP'
)