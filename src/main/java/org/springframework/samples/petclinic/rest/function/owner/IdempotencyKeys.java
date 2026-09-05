package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner id was created for a given {@code Idempotency-Key}, so a repeated
 * create with the same key returns the original owner instead of creating a duplicate.
 */
@Component
public class IdempotencyKeys {

    private final Map<String, Integer> byKey = new ConcurrentHashMap<>();

    public Integer find(String key) {
        return byKey.get(key);
    }

    public void record(String key, int ownerId) {
        byKey.putIfAbsent(key, ownerId);
    }
}
