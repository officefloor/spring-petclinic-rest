package org.springframework.samples.petclinic.rest.idempotency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a given {@code Idempotency-Key} first created, so a repeated
 * create carrying an already-seen key can return the original owner instead of making a
 * duplicate. Keyed on the raw header value; the mapping is to the created owner's id, so
 * the current owner state is always re-read from the repository rather than served stale.
 *
 * <p>Kept as a process-wide singleton (keys are unique per client request), and safe for
 * concurrent creates via {@link ConcurrentHashMap}.
 */
@Component
public class OwnerIdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner first created under {@code key}, or {@code null} if unseen. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Record the owner created under {@code key}; the first create under a key wins. */
    public void record(String key, int ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
