package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyKey;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Entry point of the create pipeline: inspects the optional {@code Idempotency-Key} header.
 *
 * <ul>
 * <li>If the key was already used by a completed create, takes the {@code replay} branch, passing
 * the originally created owner's id so it can be returned with 200 — no duplicate is created.</li>
 * <li>Otherwise takes the {@code proceed} branch (the normal create pipeline) and publishes the key
 * so {@link RecordIdempotencyKey} can remember the id the create produces.</li>
 * </ul>
 *
 * <p>The header is optional: with no (or a blank) key the create proceeds unremembered, exactly as
 * before idempotency existed.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface ProceedFlow {
        void flow();
    }

    @FunctionalInterface
    public interface ReplayFlow {
        void flow(Integer ownerId);
    }

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore store, Out<IdempotencyKey> keyOut,
            @Flow("proceed") ProceedFlow proceed, @Flow("replay") ReplayFlow replay) {
        String normalized = (key == null || key.isBlank()) ? null : key;
        keyOut.set(new IdempotencyKey(normalized));
        if (normalized != null) {
            Integer existingId = store.lookup(normalized);
            if (existingId != null) {
                replay.flow(existingId); // return the original owner, skip the create
                return;
            }
        }
        proceed.flow();
    }
}
