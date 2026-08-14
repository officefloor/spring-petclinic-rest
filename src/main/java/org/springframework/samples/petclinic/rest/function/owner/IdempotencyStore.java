package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers, for {@code POST /api/owners}, which {@code Idempotency-Key} has already produced a
 * created owner, so a repeat of the same create (same key) can replay the original owner instead of
 * creating a duplicate.
 *
 * <p>An application-scoped Spring singleton holding an in-memory {@code key -> ownerId} map. Keys are
 * client-supplied and recorded once per successful create (see {@link RecordIdempotencyKey}); the
 * first-seen mapping wins so a concurrent repeat cannot overwrite it.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created under {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return key == null ? null : this.keyToOwnerId.get(key);
    }

    /** Record {@code key -> ownerId} for a freshly created owner; the first mapping for a key wins. */
    public void record(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            this.keyToOwnerId.putIfAbsent(key, ownerId);
        }
    }
}
