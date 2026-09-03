package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.stereotype.Component;

/**
 * Remembers the owner first created under each {@code Idempotency-Key}, so a repeated
 * create with an already-seen key can replay the original result instead of creating a
 * duplicate. Process-wide and thread-safe; keys are supplied by the client.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, OwnerDto> byKey = new ConcurrentHashMap<>();

    /** The owner already created under {@code key}, or {@code null} if the key is new. */
    public OwnerDto find(String key) {
        return byKey.get(key);
    }

    /** Record the owner created under {@code key} (first writer wins). */
    public void record(String key, OwnerDto owner) {
        byKey.putIfAbsent(key, owner);
    }
}
