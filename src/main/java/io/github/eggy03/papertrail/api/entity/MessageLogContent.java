package io.github.eggy03.papertrail.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "message_log_content_table", schema = "public", indexes = {@Index(name = "idx_message_created_at", columnList = "created_at")})
public class MessageLogContent {

    @Id
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "message_content", length = 4000, nullable = false)
    private String messageContent;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

}
