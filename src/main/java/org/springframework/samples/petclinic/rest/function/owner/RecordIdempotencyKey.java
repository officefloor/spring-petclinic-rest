package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Records the {@code Idempotency-Key} → created-owner mapping once the owner has been
 * saved (and so has a generated id). Runs only on the create path; a later repeat with the
 * same key is then served directly by {@link CheckIdempotencyKey}.
 *
 * <p>When the request carries no {@code Idempotency-Key} header, there is nothing to
 * remember and the step does nothing.
 */
public class RecordIdempotencyKey {

    public void service(@Val Owner owner,
            @RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore store) {
        if (key != null && !key.isBlank()) {
            store.record(key, owner.getId());
        }
    }
}
