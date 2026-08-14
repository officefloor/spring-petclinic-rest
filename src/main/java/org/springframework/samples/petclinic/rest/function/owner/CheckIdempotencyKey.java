package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * First step of {@code POST /api/owners}. Reads the optional {@code Idempotency-Key} header and, when
 * present and already seen from a prior successful create, short-circuits the pipeline by throwing
 * {@link IdempotentReplayException} — handled to replay the originally created owner with 200 rather
 * than validating and creating a duplicate (which would otherwise be a 409 identity conflict).
 *
 * <p>Runs before any validation or duplicate check so a genuine repeat never reaches those steps. The
 * key (present or absent) is published for {@link RecordIdempotencyKey}, which records it against the
 * new owner id once the create succeeds.
 */
public class CheckIdempotencyKey {

    public void service(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            IdempotencyStore store, Out<IdempotencyKey> keyOut) throws IdempotentReplayException {
        keyOut.set(new IdempotencyKey(idempotencyKey));
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer existingOwnerId = store.find(idempotencyKey);
            if (existingOwnerId != null) {
                throw new IdempotentReplayException(existingOwnerId);
            }
        }
    }
}
