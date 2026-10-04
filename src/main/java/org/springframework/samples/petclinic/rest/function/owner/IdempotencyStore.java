package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each {@code Idempotency-Key} seen on {@code POST /owners}, so a
 * repeat create with an already-seen key replays the original result instead of creating a
 * duplicate (see {@link CheckIdempotencyKey} and {@link RecordIdempotencyKey}).
 *
 * <p>Each entry is the response representation returned the first time, kept in memory keyed by the
 * header value. The cached data is an application-level record of past outcomes, not request state:
 * it is never rolled back with the request transaction, which is what lets a replay return the
 * original response verbatim without re-reading the database.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, OwnerDto> owners = new ConcurrentHashMap<>();

    /**
     * @param key idempotency key.
     * @return the owner originally created for {@code key}, or {@code null} if the key is new.
     */
    public OwnerDto find(String key) {
        return this.owners.get(key);
    }

    /**
     * Remembers the owner created for {@code key}. The first creation for a key wins; a concurrent
     * repeat does not overwrite it.
     *
     * @param key   idempotency key.
     * @param owner response representation of the created owner.
     */
    public void record(String key, OwnerDto owner) {
        this.owners.putIfAbsent(key, owner);
    }
}
