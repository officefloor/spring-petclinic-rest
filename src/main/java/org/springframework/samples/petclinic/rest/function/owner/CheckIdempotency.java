package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * First step of {@code POST /api/owners}: honours the optional {@code Idempotency-Key} header. When
 * the request carries a key already seen for a completed create, this is a repeat, so it takes the
 * {@code existing} branch (passing the originally created owner id) which responds with that owner and
 * 200 instead of creating a duplicate. Otherwise it takes the {@code proceed} branch into the normal
 * create pipeline; a request with no key always proceeds. The key is recorded once the create
 * succeeds (see {@link RecordIdempotency}).
 */
public class CheckIdempotency {

    /** Continue into the normal create pipeline. */
    @FunctionalInterface
    public interface Proceed {
        void proceed();
    }

    /** Short-circuit to the originally created owner, identified by its id. */
    @FunctionalInterface
    public interface Existing {
        void existing(Integer ownerId);
    }

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            IdempotencyStore store, @Flow("proceed") Proceed proceed, @Flow("existing") Existing existing) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer existingId = store.find(idempotencyKey);
            if (existingId != null) {
                existing.existing(existingId);
                return;
            }
        }
        proceed.proceed();
    }
}
