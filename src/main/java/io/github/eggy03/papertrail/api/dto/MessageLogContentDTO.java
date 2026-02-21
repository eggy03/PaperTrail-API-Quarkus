package io.github.eggy03.papertrail.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class MessageLogContentDTO {

    @NotNull(message = "MessageID cannot be null")
    private Long messageId;

    @NotNull(message = "Message content cannot be null")
    private String messageContent;

    @NotNull(message = "AuthorID cannot be null")
    private Long authorId;
}
