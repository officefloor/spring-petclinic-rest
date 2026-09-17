package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner an {@code Idempotency-Key} first created, so a repeated create carrying an
 * already-seen key replays the original owner instead of inserting a duplicate. Keyed on the raw
 * header value; the stored value is the generated owner id. {@link #record} keeps the first winner,
 * so concurrent retries all resolve to the same owner. Application-scoped and in-memory: the mapping
 * outlives a single request but is not itself persisted.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner first created under {@code key}, or empty when the key is new. */
    public Optional<Integer> lookup(String key) {
        return Optional.ofNullable(ownerIdByKey.get(key));
    }

    /** Records the owner created under {@code key}, keeping the first winner for repeated keys. */
    public void record(String key, int ownerId) {
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
