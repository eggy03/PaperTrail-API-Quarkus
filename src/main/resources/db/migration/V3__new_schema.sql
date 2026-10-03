-- Rename audit_log_table to guild_table
alter table papertrailbot.audit_log_table
    rename to guild_table;

-- Rename audit_log_table.channel_id to guild_event_channel_id
alter table papertrailbot.guild_table
    rename column channel_id to guild_event_channel_id;

-- Remove not-null from guild_event_channel_id
alter table papertrailbot.guild_table
    alter column guild_event_channel_id drop not null;

-- Add member_event_channel_id to guild_table
alter table papertrailbot.guild_table
    add column member_event_channel_id bigint unique;

-- Add message_event_channel_id to guild_table
alter table papertrailbot.guild_table
    add column message_event_channel_id bigint unique;


-- Copy the contents of guild_table.guild_event_channel_id to guild_table.member_event_channel_id
update papertrailbot.guild_table
set member_event_channel_id = guild_event_channel_id;


-- Copy the contents of message_log_registration_table.channel_id to guild_table.message_event_channel_id
update papertrailbot.guild_table g
set message_event_channel_id = r.channel_id from papertrailbot.message_log_registration_table r
where g.guild_id = r.guild_id;

-- Delete message_log_registration_table
drop table papertrailbot.message_log_registration_table;


-- Rename message_content_table to message_table
alter table papertrailbot.message_log_content_table
    rename to message_table;