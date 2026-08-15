package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Entry step of {@code POST /api/owners}. Reads the optional {@code Idempotency-Key} request header
 * and routes the request:
 *
 * <ul>
 * <li>if the header is present and its key was already used to create an owner (see
 * {@link IdempotencyStore}), take the {@code existing} branch — passing the original owner id — so
 * the create is replayed as a 200 rather than duplicating the owner; otherwise</li>
 * <li>take the {@code create} branch to run the normal validation-and-create pipeline.</li>
 * </ul>
 *
 * <p>The key (empty when absent) is published so {@link RecordIdempotencyKey} can remember it once
 * the owner is saved. Routing via exactly one of two outputs — with no {@code next:} — guarantees a
 * single response per request.
 */
public class RouteIdempotentCreate {

    private static final String HEADER = "Idempotency-Key";

    /** Runs the standard create pipeline. */
    @FunctionalInterface
    public interface CreateFlow {
        void proceed();
    }

    /** Replays the owner already created for the key, responding 200. */
    @FunctionalInterface
    public interface ExistingFlow {
        void replay(Integer ownerId);
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            Out<String> idempotencyKey, @Flow("create") CreateFlow create,
            @Flow("existing") ExistingFlow existing) {
        String key = readKey(connection);
        idempotencyKey.set(key == null ? "" : key);
        if (key != null) {
            Integer ownerId = store.find(key);
            if (ownerId != null) {
                existing.replay(ownerId);
                return;
            }
        }
        create.proceed();
    }

    private static String readKey(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
