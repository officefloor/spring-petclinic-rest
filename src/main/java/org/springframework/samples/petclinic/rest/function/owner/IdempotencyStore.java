package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner id created for each {@code Idempotency-Key} seen on {@code POST /api/owners},
 * so a repeated create with an already-seen key returns the original owner instead of a duplicate.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    public Integer find(String key) {
        return this.keyToOwnerId.get(key);
    }

    public void record(String key, Integer ownerId) {
        this.keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
