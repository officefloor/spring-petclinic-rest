package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner an {@code Idempotency-Key} first created, so a create that repeats with an
 * already-seen key can return the original owner instead of storing a duplicate.
 *
 * <p>A process-wide singleton (component-scanned Spring bean), keyed by the raw header value and
 * holding the created owner's id. The first create for a key wins ({@link #record} never overwrites
 * an existing mapping), so a genuine retry always resolves back to the same owner.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id first created under {@code key}, or {@code null} if the key has not been seen. */
    public Integer lookup(String key) {
        return this.keyToOwnerId.get(key);
    }

    /** Record the owner created for {@code key}, keeping the first create's id on a repeat. */
    public void record(String key, Integer ownerId) {
        this.keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
