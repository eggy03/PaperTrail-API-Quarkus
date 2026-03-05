package io.github.eggy03.papertrail.api.service.locks;

import io.github.eggy03.papertrail.api.dto.MessageLogContentDTO;
import io.github.eggy03.papertrail.api.service.MessageLogContentService;
import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.common.constraint.NotNull;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/*
Have a centralized locking mechanism to avoid concurrency issues across scaled instances.

All operations (save, update, delete, view) for the same messageId are
serialized so that only one operation executes at a time. This protects
against race conditions caused by parallel processing, transient service
degradation, or timing variance between nodes.

Operations are executed in the order in which they successfully acquire
the Redis lock.
If UPDATE or DELETE is
processed before SAVE due to event timing, that order will be serialized
as received.

Developer Notes

Locks are applied to message log content flows but not to audit or message log registration flows
for the following reasons:

1) Message log content flow is high-frequency and mutation-heavy (create/update/delete),
   which increases the probability of concurrent execution across scaled instances.
   Registration flows are typically low-volume and one-time operations,
   making concurrency races significantly less likely.

2) Message logging content flow SAVE operations do not provide immediate visual feedback.
   In contrast, ALL operations in registration flows return explicit success or failure responses to the client.
   Moderators therefore naturally wait for the results when setting up the bot,
   reducing the likelihood of overlapping operations.

   In message logging, only UPDATE and DELETE actions generate visible
   feedback, and even then moderators are not expected to monitor or immediately check
   for updated or deleted message logs.
   Because message saves and edits are individual user-driven and
   continuous, race conditions in logging flows are almost impossible to be
   detected externally.

*/
@ApplicationScoped
@IfBuildProperty(name = "message.locks.enabled", stringValue = "true")
@RequiredArgsConstructor
@Slf4j
public class LockEnabledMessageContentOperationImpl implements MessageLogContentOperation {

    private final RedissonClient redissonClient;
    private final MessageLogContentService delegate;

    @NotNull
    public MessageLogContentDTO saveMessage(@NonNull MessageLogContentDTO dto) {

        RLock rlock = redissonClient.getFairLock(dto.getMessageId().toString());
        rlock.lock();
        log.debug("Acquired SAVE lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());

        try {
            return delegate.saveMessage(dto);
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
            return delegate.getMessage(messageId);
        } finally {
            rlock.unlock();
            log.debug("Released VIEW lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());
        }
    }

    @NotNull
    public MessageLogContentDTO updateMessage(@NonNull Long messageId, @NonNull MessageLogContentDTO dto) {

        RLock rlock = redissonClient.getFairLock(dto.getMessageId().toString());
        rlock.lock();
        log.debug("Acquired UPDATE lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());

        try {
            return delegate.updateMessage(messageId, dto);
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
            delegate.deleteMessage(messageId);
        } finally {
            rlock.unlock();
            log.debug("Released DELETE lock for messageID {} with active lock count {}", rlock.getName(), rlock.getHoldCount());
        }
    }
}
