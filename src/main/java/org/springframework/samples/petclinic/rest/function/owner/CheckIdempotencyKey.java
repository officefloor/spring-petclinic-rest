package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.support.IdempotencyKeyStore;

/**
 * First step of the create-owner pipeline. When the request carries an {@code Idempotency-Key} whose
 * value was already recorded by a previous successful create, branches to {@code replay} carrying the
 * originally created owner id (responded as 200), instead of creating a duplicate. Otherwise branches
 * to {@code proceed} to run the normal create pipeline.
 *
 * <p>The key -&gt; owner id lookup is purely in-memory ({@link IdempotencyKeyStore}); the single owner
 * read is deferred to the {@code replay} responder so this step needs no transaction. A request with no
 * (or a blank) header always proceeds.
 */
public class CheckIdempotencyKey {

    public static final String HEADER = "Idempotency-Key";

    /** Branch taken when the key has already been seen; carries the originally created owner id. */
    @FunctionalInterface
    public interface Replay {
        void replay(Integer ownerId);
    }

    /** Branch taken to run the normal create pipeline. */
    @FunctionalInterface
    public interface Proceed {
        void proceed();
    }

    public void service(ServerHttpConnection connection, IdempotencyKeyStore store,
            @Flow("replay") Replay replay, @Flow("proceed") Proceed proceed) {
        String key = idempotencyKey(connection);
        if (key != null) {
            Integer ownerId = store.find(key);
            if (ownerId != null) {
                replay.replay(ownerId);
                return;
            }
        }
        proceed.proceed();
    }

    /** The non-blank {@code Idempotency-Key} header value, or {@code null} when absent or blank. */
    static String idempotencyKey(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        return (value == null || value.isBlank()) ? null : value;
    }
}
