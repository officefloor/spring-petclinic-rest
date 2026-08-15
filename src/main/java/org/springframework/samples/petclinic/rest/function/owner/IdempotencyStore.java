package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each {@code Idempotency-Key} seen by {@code POST /api/owners}, so
 * a repeated create carrying an already-seen key can return the original owner with 200 instead of
 * creating a duplicate (see {@link RouteIdempotentCreate} and {@link RecordIdempotencyKey}).
 *
 * <p>Deliberately a process-lifetime, in-memory map rather than a persisted table: the mapping is a
 * request-dedup cache, not domain state, and must survive independently of the create's database
 * transaction — a key recorded here still resolves the second request even though the first request
 * shares that request's transaction. Keys are supplied by clients and scoped only to this endpoint.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner already created for {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId}. */
    public void record(String key, Integer ownerId) {
        ownerIdByKey.put(key, ownerId);
    }
}
