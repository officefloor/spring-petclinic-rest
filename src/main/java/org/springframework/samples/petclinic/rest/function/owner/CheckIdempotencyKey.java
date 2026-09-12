package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * First step of {@code POST /api/owners}: honours the optional {@code Idempotency-Key}
 * header. When a key has already been seen (see {@link IdempotencyStore}) the originally
 * created owner is re-loaded and the {@code existing} branch responds with it (200),
 * short-circuiting validation, the duplicate checks and a second insert. Otherwise the
 * {@code proceed} branch runs the normal create pipeline.
 *
 * <p>Reads only the header, never the request body, so the downstream {@code validate}
 * step remains the single {@code @RequestBody} binder. The key is republished as a
 * variable so {@link RecordIdempotency} can associate it with the created owner after save.
 */
public class CheckIdempotencyKey {

    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            OwnerRepository ownerRepository, Out<String> idempotencyKey, Out<Owner> replayOwner,
            @Flow("existing") Continuation existing, @Flow("proceed") Continuation proceed) {

        HttpHeader header = connection.getRequest().getHeaders().getHeader(IDEMPOTENCY_KEY);
        String key = header == null ? null : header.getValue();
        idempotencyKey.set(key);

        if (key != null && !key.isBlank()) {
            Integer priorOwnerId = store.find(key);
            if (priorOwnerId != null) {
                Owner priorOwner = tryLoad(ownerRepository, priorOwnerId);
                if (priorOwner != null) {
                    replayOwner.set(priorOwner);
                    existing.run(); // replay the original create: 200 with the original owner
                    return;
                }
            }
        }

        proceed.run(); // first time this key (or no key): run the normal create pipeline
    }

    /** Re-load the previously created owner, treating a since-removed record as unseen. */
    private static Owner tryLoad(OwnerRepository ownerRepository, Integer ownerId) {
        try {
            return ownerRepository.findById(ownerId);
        }
        catch (RuntimeException ex) {
            return null; // e.g. ObjectRetrievalFailureException — fall through to a fresh create
        }
    }
}
