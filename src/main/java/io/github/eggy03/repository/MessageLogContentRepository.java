package io.github.eggy03.repository;

import io.github.eggy03.entity.MessageLogContent;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageLogContentRepository implements PanacheRepositoryBase<MessageLogContent, Long> {
}
