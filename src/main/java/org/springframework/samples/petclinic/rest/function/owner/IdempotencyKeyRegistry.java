package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which {@code Idempotency-Key} header values have already produced an owner, mapping each
 * seen key to the id of the owner the original create persisted.
 *
 * <p>A Spring singleton (so it outlives a single request), backed by a concurrent map. Used by
 * {@link CheckIdempotencyKey} to detect a repeat create and by {@link RecordIdempotencyKey} to
 * remember the key once the owner has been saved. The store deliberately lives outside the request
 * transaction: a key stays "seen" for the life of the application, independent of any one request's
 * commit or rollback.
 */
@Component
public class IdempotencyKeyRegistry {

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /**
     * The id of the owner originally created for this key, or {@code null} if the key is unknown
     * (or {@code null}/absent).
     */
    public Integer find(String idempotencyKey) {
        return idempotencyKey == null ? null : this.keyToOwnerId.get(idempotencyKey);
    }

    /**
     * Records that {@code idempotencyKey} created the owner with {@code ownerId}. The first key wins,
     * so a later concurrent create with the same key does not overwrite the original. A {@code null}
     * or blank key (no header supplied) is ignored.
     */
    public void record(String idempotencyKey, Integer ownerId) {
        if (idempotencyKey != null && !idempotencyKey.isBlank() && ownerId != null) {
            this.keyToOwnerId.putIfAbsent(idempotencyKey, ownerId);
        }
    }
}
