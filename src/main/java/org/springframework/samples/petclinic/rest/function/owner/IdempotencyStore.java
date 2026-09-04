package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each {@code Idempotency-Key} seen on
 * {@code POST /api/owners}, so a repeat with a known key can return the original
 * owner instead of creating a duplicate.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    public Integer find(String key) {
        return this.keyToOwnerId.get(key);
    }

    public void record(String key, Integer ownerId) {
        this.keyToOwnerId.put(key, ownerId);
    }
}
