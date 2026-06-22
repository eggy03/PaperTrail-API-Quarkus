package io.github.eggy03.api.repository;

import io.github.eggy03.api.entity.SampleEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleRepository implements PanacheRepositoryBase<SampleEntity, Long> {
}
