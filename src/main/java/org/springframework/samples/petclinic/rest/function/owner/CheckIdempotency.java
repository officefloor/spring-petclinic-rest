package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * First step of create-owner. Publishes the request's {@code Idempotency-Key} for
 * {@link RecordIdempotency}, and short-circuits with an {@link IdempotentReplayException}
 * when that key already created an owner.
 */
public class CheckIdempotency {

    public void service(@HttpHeaderParameter("Idempotency-Key") String key,
            IdempotencyKeys keys, Out<String> idempotencyKey) throws IdempotentReplayException {
        idempotencyKey.set(key);
        if (key != null) {
            Integer ownerId = keys.find(key);
            if (ownerId != null) {
                throw new IdempotentReplayException(ownerId);
            }
        }
    }
}
