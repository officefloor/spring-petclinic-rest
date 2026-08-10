package org.springframework.samples.petclinic.rest.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Records the outcome of a create so a repeated request carrying the same {@code Idempotency-Key}
 * can replay the originally created owner instead of creating a duplicate.
 *
 * <p>The mapping is idempotency key -&gt; created owner id. {@link #record} keeps the first id seen
 * for a key ({@code putIfAbsent}), so concurrent repeats of the same key all resolve to the owner
 * created first.
 */
@Component
public class IdempotencyKeyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** @return the owner id previously created for {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Record the owner created for {@code key}; the first id seen for a key wins. */
    public void record(String key, Integer ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
