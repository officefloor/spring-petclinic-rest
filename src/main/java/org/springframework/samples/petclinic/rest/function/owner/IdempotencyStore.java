package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers, per {@code Idempotency-Key}, the id of the owner that a create request originally
 * produced. {@code POST /api/owners} consults it before doing any work: a key already present means
 * the create is a repeat, so the original owner is returned (200) rather than a duplicate being made.
 *
 * <p>A process-local {@link ConcurrentHashMap} is sufficient for this single-node sample — the
 * mapping only needs to outlive the individual request, not the JVM.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner previously created for {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Remember that {@code key} created the owner with {@code ownerId}. First writer wins. */
    public void record(String key, int ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
