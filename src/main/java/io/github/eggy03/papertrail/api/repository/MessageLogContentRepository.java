package io.github.eggy03.papertrail.api.repository;

import io.github.eggy03.papertrail.api.entity.MessageLogContent;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageLogContentRepository implements PanacheRepositoryBase<MessageLogContent, Long> {
}
