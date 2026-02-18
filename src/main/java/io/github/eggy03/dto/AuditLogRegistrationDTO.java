package io.github.eggy03.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AuditLogRegistrationDTO {

    @NotNull(message = "GuildID cannot be null")
    @Positive(message = "GuildID must be positive")
    private Long guildId;

    @NotNull(message = "ChannelID cannot be null")
    @Positive(message = "ChannelID must be positive")
    private Long channelId;
}