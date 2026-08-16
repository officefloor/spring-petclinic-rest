package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a create request with a given {@code Idempotency-Key} produced, so a
 * repeat of that create (same key) can return the original owner instead of creating a duplicate.
 *
 * <p>A process-wide singleton keyed by the client-supplied key; the value is the persisted owner
 * id, re-loaded fresh on replay so the caller always sees the current state. Keys are only
 * recorded after a create succeeds ({@link RecordIdempotentCreate}), so a rejected create leaves
 * no entry and can be retried.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id previously created for {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return ownerIdByKey.get(key);
    }

    /** Records that {@code key} produced the owner with {@code ownerId}. First writer wins. */
    public void record(String key, Integer ownerId) {
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
