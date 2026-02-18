package io.github.eggy03.repository;

import io.github.eggy03.entity.AuditLogRegistration;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditLogRegistrationRepository implements PanacheRepositoryBase<AuditLogRegistration, Long> {
}
