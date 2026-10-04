package org.springframework.samples.petclinic.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which entity a given client-supplied {@code Idempotency-Key} has already created, so a
 * repeated create carrying the same key can return the originally created entity instead of
 * creating a duplicate.
 *
 * <p>The store owns the keying and concurrency concerns on behalf of its callers; it is the single
 * place to evolve this behaviour later (e.g. TTL/eviction or cross-restart persistence) without
 * touching the request handlers that use it. The current implementation keeps the mapping in memory.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> keyToId = new ConcurrentHashMap<>();

    /** The id previously recorded for {@code key}, or empty if the key has not been seen. */
    public Optional<Integer> find(String key) {
        return Optional.ofNullable(keyToId.get(key));
    }

    /** Remember that {@code key} created the entity with the given {@code id}. */
    public void record(String key, Integer id) {
        keyToId.put(key, id);
    }
}
