package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * First step of {@code POST /api/owners}, ahead of any validation or persistence. When the request
 * carries an {@code Idempotency-Key} that has already produced an owner it raises
 * {@link IdempotentReplayException}, short-circuiting the pipeline so the original owner is returned
 * with 200 rather than the create running again (which would otherwise be rejected as a 409
 * duplicate). Otherwise it publishes the key for {@link RecordIdempotencyKey} to store once the
 * owner has been created.
 *
 * <p>The header is optional: a request without one (or with a blank one) simply proceeds as an
 * ordinary create and is never recorded.
 */
public class CheckIdempotencyKey {

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            IdempotencyStore store, Out<String> keyOut) throws IdempotentReplayException {
        String key = (idempotencyKey == null || idempotencyKey.isBlank()) ? "" : idempotencyKey;
        if (!key.isEmpty()) {
            Integer existingId = store.find(key);
            if (existingId != null) {
                throw new IdempotentReplayException(existingId);
            }
        }
        // Publish for RecordIdempotencyKey; empty means "no key", which it skips.
        keyOut.set(key);
    }
}
