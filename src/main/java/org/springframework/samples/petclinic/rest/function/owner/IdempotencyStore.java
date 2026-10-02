package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner id a given {@code Idempotency-Key} first created, so a repeated create carrying an
 * already-seen key can return the original owner instead of creating a duplicate.
 *
 * <p>A process-wide, in-memory map keyed on the raw header value. The first create for a key records its
 * assigned owner id ({@link #record}); a later create with the same key finds that id ({@link #find}) and
 * replays the stored owner. Writes are first-wins ({@code putIfAbsent}) so the originally created owner is
 * the one returned.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id previously created for {@code key}, or {@code null} if the key has not been seen. */
    public Integer find(String key) {
        return key == null ? null : ownerIdByKey.get(key);
    }

    /** Record (first-wins) that {@code key} created the owner with {@code ownerId}. */
    public void record(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            ownerIdByKey.putIfAbsent(key, ownerId);
        }
    }
}
