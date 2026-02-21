package io.github.eggy03.papertrail.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data

public class MessageLogContentDTO {

    @NotNull(message = "MessageID cannot be null")
    @Positive(message = "MessageID must be positive")
    private Long messageId;

    @NotNull(message = "Message content cannot be null")
    @Size(max = 4000)
    private String messageContent;

    @NotNull(message = "AuthorID cannot be null")
    @Positive(message = "AuthorID must be positive")
    private Long authorId;
}
