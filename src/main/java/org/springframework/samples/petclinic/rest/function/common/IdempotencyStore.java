package org.springframework.samples.petclinic.rest.function.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.stereotype.Component;

/**
 * In-memory record of the owners created against an {@code Idempotency-Key}. A create that
 * repeats with an already-seen key replays the originally created owner (200) instead of
 * creating a duplicate. Keyed by the client-supplied key; the value is the response DTO as it
 * was first sent, so a replay is byte-for-byte the original body without touching the database.
 *
 * <p>A singleton Spring bean, so the mapping survives across requests (the create pipeline
 * records the key after saving; a later request reads it back).
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, OwnerDto> ownersByKey = new ConcurrentHashMap<>();

    /** The owner originally created for {@code key}, or {@code null} if the key is new. */
    public OwnerDto find(String key) {
        return this.ownersByKey.get(key);
    }

    /** Remember the owner created for {@code key}; the first create for a key wins. */
    public void record(String key, OwnerDto owner) {
        this.ownersByKey.putIfAbsent(key, owner);
    }
}
