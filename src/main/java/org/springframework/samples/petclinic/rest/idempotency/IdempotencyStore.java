package org.springframework.samples.petclinic.rest.idempotency;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which {@code Idempotency-Key} has already produced an owner, so a repeated
 * {@code POST /api/owners} carrying an already-seen key returns the originally created owner instead
 * of creating a duplicate.
 *
 * <p>Keyed by the raw header value; the stored value is the id of the owner first created under that
 * key. An in-memory map is sufficient here: keys are opaque client-chosen tokens and the mapping is
 * only consulted for the lifetime of the process.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created under {@code key}, or empty when the key is new. */
    public Optional<Integer> find(String key) {
        return Optional.ofNullable(this.keyToOwnerId.get(key));
    }

    /**
     * Record {@code key -> ownerId} on first use. Keeps the first winner if the same key races, so the
     * mapping is stable.
     */
    public void record(String key, int ownerId) {
        this.keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
