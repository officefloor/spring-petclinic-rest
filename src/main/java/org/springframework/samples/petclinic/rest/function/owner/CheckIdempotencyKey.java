package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Runs first in the {@code POST /api/owners} pipeline to make the create idempotent. When
 * the request carries an {@code Idempotency-Key} that a previous create already used, it
 * takes the {@code replay} branch — passing the original owner id on so it is loaded and
 * returned with 200 — instead of building and saving a duplicate (which would otherwise be
 * a 409).
 *
 * <p>A request with no key, or with a key not seen before, calls no branch and falls
 * through to the normal validate/build/save pipeline; a new key is remembered after the
 * save by {@link RecordIdempotencyKey}.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface Replay {
        void replay(Integer ownerId);
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            @Flow("replay") Replay replay) {
        String key = IdempotencyKeyHeader.read(connection);
        if (key == null) {
            return; // no key: create normally
        }
        Integer ownerId = store.find(key);
        if (ownerId != null) {
            replay.replay(ownerId); // key already used: replay the original owner
        }
        // new key: fall through to create; recorded after the save
    }
}
