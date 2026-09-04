package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a given {@code Idempotency-Key} first created, so a repeated
 * create with the same key can return the original owner instead of a duplicate.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    public Integer find(String key) {
        return ownerIdByKey.get(key);
    }

    public void record(String key, int ownerId) {
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
