package org.springframework.samples.petclinic.rest.idempotency;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner was created for a given {@code Idempotency-Key}. When a create repeats
 * with an already-seen key, the caller can replay the original owner instead of creating a
 * duplicate.
 */
@Component
public class IdempotentOwnerCreationStore {

    private final ConcurrentMap<String, Integer> ownerIdsByKey = new ConcurrentHashMap<>();

    /** The id of the owner previously created for {@code key}, if any. */
    public Optional<Integer> findOwnerId(String key) {
        return Optional.ofNullable(this.ownerIdsByKey.get(key));
    }

    /** Record that {@code ownerId} was the owner created for {@code key}. */
    public void remember(String key, Integer ownerId) {
        this.ownerIdsByKey.putIfAbsent(key, ownerId);
    }
}
