package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each {@code Idempotency-Key} seen on the create-owner endpoint,
 * so that a create repeated with an already-seen key can return the originally created owner instead
 * of creating a duplicate.
 *
 * <p>The mapping is held in memory: a key that has never been recorded returns no id, and once a
 * successful create has associated a key with an owner id every later create carrying that same key
 * resolves to the same owner.
 */
@Component
public class IdempotencyKeyRegistry {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /**
     * Returns the id of the owner previously created for the given key, or {@code null} when the key
     * has not been seen (or was blank/absent).
     *
     * @param key the raw {@code Idempotency-Key} header value, possibly {@code null}
     * @return the associated owner id, or {@code null} when none is recorded
     */
    public Integer findOwnerId(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return this.ownerIdByKey.get(key);
    }

    /**
     * Records that the given key produced the owner with the given id. Blank or absent keys are
     * ignored. The first record for a key wins, so a repeated create never overwrites the original.
     *
     * @param key     the raw {@code Idempotency-Key} header value, possibly {@code null}
     * @param ownerId the id of the owner created for that key
     */
    public void record(String key, Integer ownerId) {
        if (key == null || key.isBlank() || ownerId == null) {
            return;
        }
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
