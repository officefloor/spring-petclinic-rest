package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * First step of {@code POST /api/owners}: honours an optional {@code Idempotency-Key} header.
 *
 * <p>When the header is absent the value binds to {@code null} and the create proceeds normally.
 * When it carries a key already seen (an owner was previously created under it), the pipeline
 * short-circuits by throwing {@link IdempotentReplayException}, which
 * {@code IdempotentReplayExceptionHandler} turns into a 200 replay of the original owner &mdash; so a
 * repeated create never duplicates. A new key falls through to the create pipeline;
 * {@link RecordIdempotencyKey} remembers it once the owner is saved.
 */
public class CheckIdempotencyKey {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            IdempotencyStore store) throws IdempotentReplayException {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return; // no key: ordinary create
        }
        Integer existingOwnerId = store.find(idempotencyKey).orElse(null);
        if (existingOwnerId != null) {
            throw new IdempotentReplayException(existingOwnerId);
        }
    }
}
