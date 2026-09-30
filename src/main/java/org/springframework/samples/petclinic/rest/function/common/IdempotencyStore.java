package org.springframework.samples.petclinic.rest.function.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each {@code Idempotency-Key} seen on {@code POST /api/owners},
 * so a repeated create with an already-seen key returns the originally created owner instead of
 * creating a duplicate.
 *
 * <p>A process-wide, in-memory map: the mapping is deliberately kept outside the request's
 * transaction so that once the first create commits its key, a retry finds it even though each
 * request is its own transaction. {@link #remember(String, Integer)} keeps the first id stored
 * for a key ({@code putIfAbsent}), so the returned owner is always the one originally created.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id originally created for {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return key == null ? null : ownerIdByKey.get(key);
    }

    /** Record the owner created for {@code key}, keeping the first id if the key is already known. */
    public void remember(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            ownerIdByKey.putIfAbsent(key, ownerId);
        }
    }
}
