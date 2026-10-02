package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Process-wide store mapping a client-supplied {@code Idempotency-Key} to the id of the owner
 * created under it. {@link CheckIdempotency} consults it so a create that repeats with an
 * already-seen key returns the originally created owner with 200 instead of creating a
 * duplicate; {@link RecordIdempotency} populates it once the owner is saved.
 *
 * <p>A Spring bean (injected by type into the functions), so the one instance is shared across
 * requests. Kept in memory: the mapping is only meaningful while the referenced owner still
 * exists, and {@link CheckIdempotency} re-checks existence before treating an entry as a hit,
 * so a stale entry simply falls through to a normal create.
 *
 * <p>Callers pass a present (non-blank) key — the null/blank rule lives on {@link IdempotencyKey}.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id previously created under {@code key}, or {@code null} if none. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId}. */
    public void record(String key, int ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
