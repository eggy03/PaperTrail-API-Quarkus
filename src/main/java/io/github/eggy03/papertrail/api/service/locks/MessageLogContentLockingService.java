package io.github.eggy03.papertrail.api.service.locks;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import io.github.eggy03.papertrail.api.service.MessageLogContentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/*
Have a centralized locking mechanism to avoid concurrency issues during high volume message logging

While this solves for concurrency issues where an UPDATE/DELETE is being performed while a SAVE
is being processed, it cannot solve the ORDERING ISSUE.
That is, if for some network or performance reason an UPDATE takes the lock before SAVE,
the UPDATE will just silently fail cause there is NOTHING to update and the OLD state will be SAVED instead,
causing STALE data.

Retry logic on update and save MAY help, but it's too much boilerplate.
Kafka probably is the best solution, but I don't want to add the complexity of an entirely new system.

Usually, this should be a one-in-a-million issue unless your service health is really tanking, in which case
maybe vertical or horizontal scaling would help more to keep up with the resource pressure
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MessageLogContentLockingService {

    private final RedissonClient redissonClient;
    private final MessageLogContentService service;

    @NotNull
    public MessageLogContentDTO saveMessage(@NonNull MessageLogContentDTO dto) {

        RLock rlock = redissonClient.getFairLock(dto.getMessageId().toString());
        rlock.lock();
        log.debug("Acquired SAVE lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());

        try {
            return service.saveMessage(dto);
        } finally {
            rlock.unlock();
            log.debug("Released SAVE lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());
        }
    }

    /*
    Initially, view operations were not locked because only mutating operations
    (save, update, delete) were expected to require synchronization.

    However, Redisson connections are initialized lazily. During cold startup,
    the first save/update call blocks while Redisson establishes connections.
    This introduces significant delay before the lock is acquired.

    Clients typically call view before update. If view is not locked, it can
    execute immediately while the save operation is still waiting on Redisson
    initialization. In this case, view may return 404 because the save has not
    completed yet.

    Since some clients only proceed with update if view returns a DTO,
    the update is never scheduled when view returns 404. The save eventually
    completes, but the intended update is skipped.

    Under rapid save/update sequences, this creates race conditions and
    inconsistent state.

    For this reason, view operations now acquire the same Redisson lock to
    maintain ordering.
    */
    @NotNull
    public MessageLogContentDTO getMessage(@NonNull Long messageId) {

        RLock rlock = redissonClient.getFairLock(messageId.toString());
        rlock.lock();
        log.debug("Acquired VIEW lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());

        try {
            return service.getMessage(messageId);
        } finally {
            rlock.unlock();
            log.debug("Released VIEW lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());
        }
    }

    @NotNull
    public MessageLogContentDTO updateMessage(@NonNull MessageLogContentDTO dto) {

        RLock rlock = redissonClient.getFairLock(dto.getMessageId().toString());
        rlock.lock();
        log.debug("Acquired UPDATE lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());

        try {
            return service.updateMessage(dto.getMessageId(), dto);
        } finally {
            rlock.unlock();
            log.debug("Released UPDATE lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());
        }
    }

    public void deleteMessage(@NonNull Long messageId) {

        RLock rlock = redissonClient.getFairLock(String.valueOf(messageId));
        rlock.lock();
        log.debug("Acquired DELETE lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());

        try {
            service.deleteMessage(messageId);
        } finally {
            rlock.unlock();
            log.debug("Released DELETE lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());
        }
    }
}
