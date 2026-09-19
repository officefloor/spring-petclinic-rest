package org.springframework.samples.petclinic.rest.idempotency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a previously-seen {@code Idempotency-Key} created, so a repeated
 * create with the same key can return the original owner instead of making a duplicate.
 * Keys are opaque client-supplied strings; the value is the created owner's id.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id previously created under {@code key}, or {@code null} if unseen. */
    public Integer find(String key) {
        return ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId}. */
    public void record(String key, Integer ownerId) {
        ownerIdByKey.put(key, ownerId);
    }
}
