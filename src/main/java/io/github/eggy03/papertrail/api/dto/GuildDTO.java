package io.github.eggy03.papertrail.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuildDTO {

    @NotNull(message = "GuildID cannot be null")
    @Positive(message = "GuildID must be positive")
    private Long guildId;

    @Positive(message = "Guild Event ChannelID must be positive")
    private Long guildEventChannelId;

    @Positive(message = "Member Event ChannelID must be positive")
    private Long memberEventChannelId;

    @Positive(message = "Message Event ChannelID must be positive")
    private Long messageEventChannelId;
}