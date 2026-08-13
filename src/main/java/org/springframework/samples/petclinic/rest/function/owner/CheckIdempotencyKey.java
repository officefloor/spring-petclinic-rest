package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * First step of {@code POST /api/owners}: if the request carries an {@code Idempotency-Key} that has
 * already created an owner, short-circuits the pipeline with an {@link IdempotentReplayException}
 * (handled as a 200 returning the original owner) rather than creating a duplicate. A request with no
 * key, or an unseen key, falls through to the normal create pipeline. Reads only the header — not the
 * body — so the body remains free for {@link NormalizeOwnerAddress} to bind once.
 */
public class CheckIdempotencyKey {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            IdempotencyStore store) throws IdempotentReplayException {
        Integer existingOwnerId = store.find(idempotencyKey);
        if (existingOwnerId != null) {
            throw new IdempotentReplayException(existingOwnerId);
        }
    }
}
