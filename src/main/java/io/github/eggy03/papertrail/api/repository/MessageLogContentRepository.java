package io.github.eggy03.papertrail.api.repository;

import io.github.eggy03.papertrail.api.entity.MessageLogContent;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;

@ApplicationScoped
public class MessageLogContentRepository implements PanacheRepositoryBase<MessageLogContent, Long> {

    public long deleteOlderThan(OffsetDateTime cutOff) {
        return delete("createdAt < ?1", cutOff);
    }
}
