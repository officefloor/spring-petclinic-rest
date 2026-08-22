package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each {@code Idempotency-Key} seen on {@code POST /api/owners},
 * so a repeat create with an already-seen key can replay the original owner instead of creating a
 * duplicate. Keyed on the raw header value; the recorded value is the created owner's id.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> owners = new ConcurrentHashMap<>();

    /** The id of the owner created under {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return this.owners.get(key);
    }

    /** Remember, for a first-seen {@code key}, the id of the owner it created. */
    public void record(String key, int ownerId) {
        this.owners.putIfAbsent(key, ownerId);
    }
}
