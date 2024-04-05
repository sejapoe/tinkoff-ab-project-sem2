alter table images
    add column user_id bigint null references users (id) on delete cascade;