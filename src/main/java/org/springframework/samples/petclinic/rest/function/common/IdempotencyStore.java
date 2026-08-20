package org.springframework.samples.petclinic.rest.function.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for a given {@code Idempotency-Key}, so a repeated create with a
 * key already seen returns the originally created owner instead of creating a duplicate.
 *
 * <p>A process-wide singleton keyed by the client-supplied idempotency key; the value is the id of
 * the owner the first request created. Keys are opaque and never reused across owners, so a plain
 * concurrent map is enough — the store only ever grows and each key maps to exactly one owner id.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id previously created for {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return key == null ? null : ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId}. First writer wins. */
    public void record(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            ownerIdByKey.putIfAbsent(key, ownerId);
        }
    }
}
