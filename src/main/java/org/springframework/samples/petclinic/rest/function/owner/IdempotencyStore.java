package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers, per {@code Idempotency-Key}, the id of the owner a create request produced, so a
 * repeat of that request returns the original owner instead of creating a duplicate. A single
 * application-wide bean: keys are supplied by clients and outlive any one request, so the record
 * cannot live in a per-request pipeline variable.
 *
 * <p>{@link RecallIdempotentOwner} reads it at the start of {@code POST /api/owners};
 * {@link RecordIdempotentOwner} writes it once the owner has an id.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner previously created for {@code key}, or {@code null} if none (or no key). */
    public Integer recall(String key) {
        return key == null ? null : ownerIdByKey.get(key);
    }

    /** Record the owner created for {@code key}, keeping the first if the key is already known. */
    public void record(String key, int ownerId) {
        if (key != null) {
            ownerIdByKey.putIfAbsent(key, ownerId);
        }
    }
}
