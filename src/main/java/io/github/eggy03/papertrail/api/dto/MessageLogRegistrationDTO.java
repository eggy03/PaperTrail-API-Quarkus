package io.github.eggy03.papertrail.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageLogRegistrationDTO {

    @NotNull(message = "GuildID cannot be null")

    private Long guildId;

    @NotNull(message = "ChannelID cannot be null")
    private Long channelId;

}
