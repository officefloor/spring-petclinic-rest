package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a given {@code Idempotency-Key} already created, so a repeated
 * {@code POST /api/owners} carrying a key that has been seen replays the original owner
 * instead of creating a duplicate (see {@link CheckIdempotencyKey} and
 * {@link RecordIdempotencyKey}).
 *
 * <p>The first create for a key wins: {@link #record} never overwrites an existing
 * mapping, so a concurrent or repeated create cannot re-point a key at a different owner.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner created for {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Remember the owner {@code key} created; the first create for a key wins. */
    public void record(String key, Integer ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
