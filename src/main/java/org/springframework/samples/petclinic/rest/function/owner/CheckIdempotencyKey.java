package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Entry point for {@code POST /owners}: enforces idempotent create. When the request carries an
 * {@code Idempotency-Key} that has already produced an owner, it replays the original response
 * (200) and skips the create pipeline entirely; otherwise it hands off to the normal create flow.
 *
 * <p>Both outcomes are {@link Flow} branches and this step has no {@code next}, so exactly one of
 * them runs — invoking a flow does not fall through to the other. The replay branch carries the
 * stored {@link OwnerDto} as its argument (arriving downstream as {@code @Parameter}), so no
 * database read is needed. Runs before any step binds the request body, so the body is still read
 * exactly once, by the create flow.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface ReplayFlow {
        void replay(OwnerDto owner);
    }

    @FunctionalInterface
    public interface CreateFlow {
        void create();
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            @Flow("replay") ReplayFlow replay, @Flow("create") CreateFlow create) {
        String key = IdempotencyKey.fromRequest(connection);
        if (key != null) {
            OwnerDto existing = store.find(key);
            if (existing != null) {
                replay.replay(existing);
                return;
            }
        }
        create.create();
    }
}
