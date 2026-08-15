package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.web.HttpHeaderParameter;

/**
 * First step of {@code POST /api/owners}. Reads the optional {@code Idempotency-Key} header and, if
 * a create has already been made under that key, replays the originally created owner (a 200 via
 * {@link LoadStoredOwner} + {@link RespondWithOwner}) instead of creating a duplicate. Otherwise it
 * proceeds with the normal create pipeline (starting at {@link ValidateNewOwner}), publishing the
 * key so {@link RecordIdempotencyKey} can remember the owner that create produces.
 *
 * <p>The key is published even when absent (as an empty {@link IdempotencyKey}) so the recording
 * step always has a value to read; recording is skipped when no key was supplied.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface Replay {
        void replay(Integer ownerId);
    }

    @FunctionalInterface
    public interface Proceed {
        void proceed();
    }

    public void service(@HttpHeaderParameter("Idempotency-Key") String key, IdempotencyStore store,
            Out<IdempotencyKey> keyOut, @Flow("replay") Replay replay, @Flow("proceed") Proceed proceed) {
        keyOut.set(new IdempotencyKey(key));
        if (key != null && !key.isBlank()) {
            Integer existingOwnerId = store.find(key);
            if (existingOwnerId != null) {
                replay.replay(existingOwnerId); // repeat of a seen key: return the original owner (200)
                return;
            }
        }
        proceed.proceed(); // new (or keyless) request: run the normal create pipeline
    }
}
