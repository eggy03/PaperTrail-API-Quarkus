package io.github.eggy03.api.service;

import io.github.eggy03.api.entity.SampleEntity;
import io.github.eggy03.api.repository.SampleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public final class SampleService {

    private static final Logger log = LoggerFactory.getLogger(SampleService.class);
    private final SampleRepository repository;

    public SampleService(SampleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveSample(@NonNull SampleEntity sampleEntity) {
        log.info("Sample To Save: [{}, {}]", sampleEntity.getId(), sampleEntity.getText());
        repository.persistAndFlush(sampleEntity);
    }

    @Transactional
    public boolean deleteSample(@NonNull Long id) {
        log.info("Sample To Delete: [{}]", id);
        return repository.deleteById(id);
    }
}
