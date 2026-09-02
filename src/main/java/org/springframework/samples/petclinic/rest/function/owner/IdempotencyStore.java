package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.stereotype.Component;

/**
 * Remembers the owner first created for each {@code Idempotency-Key}, so a repeated create with an
 * already-seen key can return the original owner instead of creating a duplicate. In-memory and keyed
 * only by the client-supplied key, so distinct keys never collide.
 */
@Component
public class IdempotencyStore {

    private final Map<String, OwnerDto> byKey = new ConcurrentHashMap<>();

    /** The owner first created under {@code key}, or {@code null} if the key is blank or unseen. */
    public OwnerDto find(String key) {
        return (key == null || key.isBlank()) ? null : byKey.get(key);
    }

    /** Records the owner created under {@code key}; ignores a blank key. */
    public void record(String key, OwnerDto owner) {
        if (key != null && !key.isBlank()) {
            byKey.putIfAbsent(key, owner);
        }
    }
}
