package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;

/**
 * Entry step of create-owner: honours an optional {@code Idempotency-Key} request header so a repeat
 * of a create returns the originally created owner instead of creating a duplicate.
 *
 * <p>The key is published as an {@link IdempotencyKey} variable for {@link RecordIdempotency} and the
 * pipeline branches on it:
 * <ul>
 * <li>a key already seen (an owner was created for it) takes the {@code replay} branch, carrying the
 * stored owner id, which responds {@code 200} with that owner;</li>
 * <li>otherwise the {@code proceed} branch runs the normal create pipeline, which records the key
 * against the new owner after saving.</li>
 * </ul>
 * Reading only the header here (never the body) leaves {@code @RequestBody} for the first step of the
 * proceed branch.
 */
public class CheckIdempotency {

    @FunctionalInterface
    public interface ProceedFlow {
        void proceed();
    }

    @FunctionalInterface
    public interface ReplayFlow {
        void replay(Integer ownerId);
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            Out<IdempotencyKey> keyOut, @Flow("proceed") ProceedFlow proceed,
            @Flow("replay") ReplayFlow replay) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = header != null ? header.getValue() : null;
        keyOut.set(new IdempotencyKey(key));

        Integer existingOwnerId = store.find(key);
        if (existingOwnerId != null) {
            replay.replay(existingOwnerId);
            return;
        }
        proceed.proceed();
    }
}
