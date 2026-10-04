package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Remembers the just-created owner against the request's {@code Idempotency-Key}, so a later repeat
 * with the same key replays this result (see {@link CheckIdempotencyKey}) instead of creating a
 * duplicate. A no-op when no key was supplied.
 *
 * <p>Runs after {@link SaveOwner} so the generated id is present, and stores the same
 * representation {@link RespondWithOwnerCreated} returns. Governed by the write transaction like
 * the surrounding steps, so the pipeline's governance span stays unbroken and the request still
 * commits once at the end.
 */
public class RecordIdempotencyKey {

    public void service(ServerHttpConnection connection, @Val Owner owner,
            OwnerMapper ownerMapper, IdempotencyStore store) {
        String key = IdempotencyKey.fromRequest(connection);
        if (key != null) {
            store.record(key, ownerMapper.toOwnerDto(owner));
        }
    }
}
