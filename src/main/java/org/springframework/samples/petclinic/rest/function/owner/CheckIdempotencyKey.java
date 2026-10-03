package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.service.IdempotencyStore;

/**
 * First step of the create pipeline. When the request carries an {@code Idempotency-Key}
 * that was already seen, the owner created the first time is published and the {@code replay}
 * branch is taken to respond 200 with it, instead of continuing on to create a duplicate. A
 * missing or new key takes the {@code proceed} branch and falls through to validation.
 *
 * <p>The replayed owner is published as the pipeline's {@link Owner} so the shared responder
 * maps and sends it, rather than duplicating that logic here.
 */
public class CheckIdempotencyKey {

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            OwnerRepository ownerRepository, Out<Owner> ownerOut,
            @Flow("replay") PipelineBranch replay, @Flow("proceed") PipelineBranch proceed) {
        Integer id = store.find(IdempotencyStore.keyFrom(connection));
        if (id != null) {
            try {
                Owner owner = ownerRepository.findById(id);
                if (owner != null) {
                    ownerOut.set(owner);
                    replay.take();
                    return;
                }
            }
            catch (DataAccessException ex) {
                // The recorded owner is gone; treat the key as new and create afresh.
            }
        }
        proceed.take();
    }
}
