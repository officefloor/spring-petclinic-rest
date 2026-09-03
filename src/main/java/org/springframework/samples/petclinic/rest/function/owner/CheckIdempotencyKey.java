package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Entry step for {@code POST /api/owners}. When the request carries an
 * {@code Idempotency-Key} already seen, replay the originally created owner; otherwise
 * run the normal create pipeline.
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

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore idempotencyStore, @Flow("create") CreateFlow create,
            @Flow("replay") ReplayFlow replay) {
        Integer existing = key == null ? null : idempotencyStore.find(key);
        if (existing != null) {
            replay.replay(existing);
        }
        else {
            create.create();
        }
    }
}
