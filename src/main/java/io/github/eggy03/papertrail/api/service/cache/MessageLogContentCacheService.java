package io.github.eggy03.papertrail.api.service.cache;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;

@ApplicationScoped
public class MessageLogContentCacheService {

    @CacheInvalidate(cacheName = "messageContent")
    public void invalidateCache(@NonNull @CacheKey Long messageId) {
        // this method exists simply because some of my parameters are full DTOs and Quarkus doesn't support
        // SpEL type runtime behavior or setting just guildId as a key out of my DTOs
        // invoking this will trigger an invalidation to the update logic
    }
}
