package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner each {@code Idempotency-Key} created, so a create that repeats with an
 * already-seen key returns the originally created owner (200) instead of creating a duplicate.
 *
 * <p>An in-memory, process-wide map keyed on the raw header value: the create pipeline records the
 * new owner's id under the key after saving, and the first step of a later create looks the key up
 * to replay the original response.
 */
@Component
public class IdempotencyRegistry {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** Owner id previously created under {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return key == null ? null : this.ownerIdByKey.get(key);
    }

    /** Remember that {@code key} created the owner with {@code ownerId}. */
    public void record(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            this.ownerIdByKey.putIfAbsent(key, ownerId);
        }
    }
}
