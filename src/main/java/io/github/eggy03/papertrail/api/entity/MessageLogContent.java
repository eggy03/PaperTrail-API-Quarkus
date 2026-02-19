package io.github.eggy03.papertrail.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "message_log_content_table", schema = "public")
public class MessageLogContent {

    @Id
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "message_content", length = 4000)
    private String messageContent;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

}
