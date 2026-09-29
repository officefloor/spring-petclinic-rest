package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers, per {@code Idempotency-Key}, the id of the owner that was originally created for that
 * key, so that a repeated create carrying an already-seen key can return the original owner instead
 * of creating a duplicate.
 *
 * <p>Backed by a simple in-memory map: adequate for a single-instance deployment and for the
 * request-repeat semantics exercised here. Keys are never reused with a different payload in this
 * application, so a plain "first create wins" record is all that is stored.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created for {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return keyToOwnerId.get(key);
    }

    /**
     * Records that {@code ownerId} was created for {@code key}, keeping the first create's id if the
     * key was already recorded (concurrent repeats collapse onto the original owner).
     */
    public void remember(String key, Integer ownerId) {
        keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
