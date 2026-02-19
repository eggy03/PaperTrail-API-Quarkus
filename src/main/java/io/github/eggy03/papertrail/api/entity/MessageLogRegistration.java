package io.github.eggy03.papertrail.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "message_log_registration_table", schema = "public")
public class MessageLogRegistration {

    @Id
    @Column(name = "guild_id")
    private Long guildId;

    @Column (name = "channel_id", unique = true, nullable = false)
    private Long channelId;

}
