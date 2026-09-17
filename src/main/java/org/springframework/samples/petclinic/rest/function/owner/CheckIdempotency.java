package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.web.HttpHeaderParameter;

/**
 * Entry step of the create pipeline, routing to exactly one of two outputs. When the request carries
 * an {@code Idempotency-Key} that has already created an owner (see {@link IdempotencyStore}), it
 * takes the {@code existing} output to replay that owner with 200, short-circuiting the create so the
 * request never reaches the duplicate checks (which would otherwise reject the repeat with 409). With
 * no key, or a key not seen before, it takes the {@code proceed} output and the normal create runs;
 * {@link RecordIdempotencyKey} then registers the key once the owner is saved.
 */
public class CheckIdempotency {

    /** Branch taken for a repeated key: replays the owner the key first created, by its id. */
    @FunctionalInterface
    public interface ExistingOwnerFlow {
        void replay(Integer ownerId);
    }

    /** Branch taken for a new (or absent) key: runs the normal create pipeline. */
    @FunctionalInterface
    public interface ProceedFlow {
        void proceed();
    }

    public void service(
            @HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            IdempotencyStore idempotencyStore, @Flow("existing") ExistingOwnerFlow existing,
            @Flow("proceed") ProceedFlow proceed) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer existingId = idempotencyStore.lookup(idempotencyKey).orElse(null);
            if (existingId != null) {
                existing.replay(existingId);
                return;
            }
        }
        proceed.proceed();
    }
}
