package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.IdempotentCreateReplayException;
import org.springframework.samples.petclinic.rest.support.IdempotencyStore;

/**
 * First step of {@code POST /api/owners}. Reads the optional {@code Idempotency-Key} header and,
 * when it names a key that already created an owner, short-circuits the whole create pipeline —
 * loading that owner and raising {@link IdempotentCreateReplayException} so the handler returns
 * it with 200 instead of creating a duplicate.
 *
 * <p>Runs before {@link ValidateOwnerFields} (which binds the request body): a replay never reads
 * the body, so the first request and its repeats are matched purely by the key. The key — absent
 * or unseen — is published for {@link RecordIdempotencyKey} to store once the create succeeds.
 * A key whose owner can no longer be found (e.g. later deleted) falls through and creates anew.
 */
public class CheckIdempotencyKey {

    public void service(ServerHttpConnection connection, IdempotencyStore idempotencyStore,
            OwnerRepository ownerRepository, Out<String> idempotencyKey)
            throws IdempotentCreateReplayException {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = header == null ? null : header.getValue();
        if (key != null && key.isBlank()) {
            key = null;
        }
        idempotencyKey.set(key);
        if (key != null) {
            Integer existingId = idempotencyStore.find(key);
            if (existingId != null) {
                Owner existing = ownerRepository.findById(existingId);
                if (existing != null) {
                    throw new IdempotentCreateReplayException(existing);
                }
            }
        }
    }
}
