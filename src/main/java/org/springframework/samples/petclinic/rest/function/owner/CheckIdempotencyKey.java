package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * First step of {@code POST /api/owners}. Reads the optional {@code Idempotency-Key} header and, when
 * it names a key already seen (a repeated create), short-circuits to replay the originally created
 * owner with 200 instead of running the create pipeline again — which would otherwise reject the
 * repeat as a duplicate (409).
 *
 * <p>Branches:
 * <ul>
 * <li>{@code replay} — the key was seen before; carries the original owner's id so
 * {@link LoadOwnerById} can load it and respond 200.</li>
 * <li>{@code fresh} — no key, or a key not seen before; runs the normal create pipeline.</li>
 * </ul>
 *
 * <p>Always republishes the key (possibly absent) as an {@link IdempotencyKey} variable so
 * {@link RecordIdempotencyKey} can record the created owner under it at the end of a fresh create.
 * Only the in-memory {@link IdempotencyStore} is consulted here (no database access), so this step
 * needs no governance.
 */
public class CheckIdempotencyKey {

    /** Replay branch: hand the originally created owner's id to the loader. */
    @FunctionalInterface
    public interface ReplayFlow {
        void replay(Integer ownerId);
    }

    /** Fresh branch: run the normal create pipeline. */
    @FunctionalInterface
    public interface FreshFlow {
        void proceed();
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            Out<IdempotencyKey> keyOut, @Flow("replay") ReplayFlow replay, @Flow("fresh") FreshFlow fresh) {

        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = header == null ? null : header.getValue();
        IdempotencyKey idempotencyKey = new IdempotencyKey(key);
        keyOut.set(idempotencyKey);

        if (idempotencyKey.isPresent()) {
            Integer ownerId = store.find(key);
            if (ownerId != null) {
                replay.replay(ownerId); // seen before: replay the original owner (200)
                return;
            }
        }
        fresh.proceed();
    }
}
