package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.support.IdempotencyStore;

/**
 * First step of the create pipeline. When the request carries an {@code Idempotency-Key}
 * header already seen for a successful create, routes to {@link RespondWithExistingOwner}
 * with the originally created owner id — a 200 replay rather than a duplicate create.
 * Otherwise (no key, or a key not yet seen) routes to the normal create pipeline; a new
 * key is remembered after the owner is saved by {@link RecordIdempotencyKey}.
 *
 * <p>Reads only the header, never the body, so {@link ValidateOwnerRequiredFields} remains
 * the sole step that binds {@code @RequestBody}.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface ExistingOwnerFlow {
        void flow(Integer ownerId);
    }

    @FunctionalInterface
    public interface ProceedFlow {
        void flow();
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            @Flow("existing") ExistingOwnerFlow existing, @Flow("proceed") ProceedFlow proceed) {
        String key = IdempotencyKeys.read(connection);
        if (key != null) {
            Integer ownerId = store.find(key);
            if (ownerId != null) {
                existing.flow(ownerId); // replay the original create with 200
                return;
            }
        }
        proceed.flow(); // fresh create — run the normal pipeline
    }
}
