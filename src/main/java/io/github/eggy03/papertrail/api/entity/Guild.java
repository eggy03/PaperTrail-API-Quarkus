package io.github.eggy03.papertrail.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "guild_table")
public class Guild {

    @Id
    @Column(name = "guild_id")
    private Long guildId;

    @Column(name = "guild_event_channel_id", unique = true)
    private Long guildEventChannelId;

    @Column(name = "member_event_channel_id", unique = true)
    private Long memberEventChannelId;

    @Column(name = "message_event_channel_id", unique = true)
    private Long messageEventChannelId;

}
