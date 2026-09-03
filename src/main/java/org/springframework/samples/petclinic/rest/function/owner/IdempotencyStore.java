package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.stereotype.Component;

/**
 * Remembers the owner first created under each {@code Idempotency-Key} so a repeated create
 * replays it instead of creating a duplicate.
 */
@Component
public class IdempotencyStore {

    private final Map<String, OwnerDto> byKey = new ConcurrentHashMap<>();

    /** The owner previously created under {@code key}, or {@code null} when the key is new. */
    public OwnerDto find(String key) {
        return this.byKey.get(key);
    }

    /** Record the created owner under {@code key}, keeping the first create when repeated. */
    public void record(String key, OwnerDto owner) {
        this.byKey.putIfAbsent(key, owner);
    }
}
