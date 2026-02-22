create table papertrailbot.audit_log_table
(
    guild_id   bigint not null,
    channel_id bigint not null unique,
    primary key (guild_id)
);

create table papertrailbot.message_log_content_table
(
    message_id      bigint                      not null,
    author_id       bigint                      not null,
    created_at      timestamp(6) with time zone not null,
    message_content varchar(4000)               not null,
    primary key (message_id)
);

create table papertrailbot.message_log_registration_table
(
    guild_id   bigint not null,
    channel_id bigint not null unique,
    primary key (guild_id)
);

create index idx_message_created_at
    on papertrailbot.message_log_content_table (created_at);
