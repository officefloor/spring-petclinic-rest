package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each seen {@code Idempotency-Key}, so a repeated
 * create with the same key replays the original owner instead of creating a duplicate.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** Owner id previously created for this key, or {@code null} if the key is new. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Record the owner created for this key. */
    public void record(String key, Integer ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
