package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Process-wide record of the owner created for each {@code Idempotency-Key} seen on
 * {@code POST /api/owners}. A repeated create carrying an already-seen key is answered with the
 * originally created owner (see {@link CheckIdempotency}) instead of creating a duplicate; a fresh
 * key is recorded once the create succeeds (see {@link RecordIdempotency}).
 *
 * <p>Deliberately held in memory rather than the database: the mapping is a request-dedup concern,
 * not persisted owner state, and must survive independently of any single request's transaction.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** Owner id previously created for {@code key}, or {@code null} if the key is new. */
    public Integer get(String key) {
        return ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId} (first writer wins). */
    public void record(String key, int ownerId) {
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
