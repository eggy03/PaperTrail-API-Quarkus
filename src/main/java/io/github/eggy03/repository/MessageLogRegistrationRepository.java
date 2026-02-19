package io.github.eggy03.repository;

import io.github.eggy03.entity.MessageLogRegistration;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageLogRegistrationRepository implements PanacheRepositoryBase<MessageLogRegistration, Long> {
}
