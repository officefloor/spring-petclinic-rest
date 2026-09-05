package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * First step of {@code POST /api/owners}: honours the optional {@code Idempotency-Key} header.
 *
 * <p>If the request carries a key that {@link IdempotencyStore} has already seen, the original
 * owner is replayed (the {@code replay} branch responds 200) and the create pipeline is skipped
 * entirely — no duplicate is persisted. Otherwise (no key, or a first-seen key) the {@code create}
 * branch runs the normal validate&rarr;build&rarr;save pipeline; {@link RecordIdempotencyKey}
 * remembers the key once the owner has an id.
 *
 * <p>Exactly one branch is taken, so this step has no {@code next:} — each outcome is an explicit
 * flow. The replay branch passes the remembered owner id on as its {@code @Parameter}.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface CreateFlow {
        void create();
    }

    @FunctionalInterface
    public interface ReplayFlow {
        void replay(Integer ownerId);
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            @Flow("create") CreateFlow create, @Flow("replay") ReplayFlow replay) {
        String key = IdempotencyKeys.from(connection);
        Integer existingOwnerId = store.find(key);
        if (existingOwnerId != null) {
            replay.replay(existingOwnerId);
        } else {
            create.create();
        }
    }
}
