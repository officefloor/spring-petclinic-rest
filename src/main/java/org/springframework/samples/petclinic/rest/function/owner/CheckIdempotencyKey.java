package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * Entry step for {@code POST /api/owners}. Reads the optional {@code Idempotency-Key}
 * header and publishes it for later steps. When the key was already used to create an
 * owner (and that owner still exists), it republishes the original owner and takes the
 * {@code replay} branch so the endpoint returns it with 200 instead of creating a
 * duplicate. Otherwise it takes the {@code proceed} branch into the normal create pipeline.
 */
public class CheckIdempotencyKey {

    private static final String HEADER = "Idempotency-Key";

    @FunctionalInterface
    public interface Replay {
        void replay();
    }

    @FunctionalInterface
    public interface Proceed {
        void proceed();
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            OwnerRepository ownerRepository, Out<IdempotencyKey> keyOut, Out<Owner> replayOwner,
            @Flow("replay") Replay replay, @Flow("proceed") Proceed proceed) {
        IdempotencyKey key = readKey(connection);
        keyOut.set(key);
        if (key.isPresent()) {
            Integer ownerId = store.find(key.value());
            if (ownerId != null) {
                Owner existing = ownerRepository.findById(ownerId);
                if (existing != null) {
                    replayOwner.set(existing);
                    replay.replay();
                    return;
                }
            }
        }
        proceed.proceed();
    }

    private static IdempotencyKey readKey(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        return new IdempotencyKey(header == null ? null : header.getValue());
    }
}
