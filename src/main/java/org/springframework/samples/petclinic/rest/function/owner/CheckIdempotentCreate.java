package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * First step of create-owner: when the request carries an {@code Idempotency-Key} that already
 * created an owner, short-circuit the pipeline by throwing {@link IdempotentReplayException} with
 * that owner, so the request replays the original 200 response instead of creating a duplicate.
 * A request with no key, an unseen key, or a key whose owner has since vanished falls through to
 * the normal create pipeline.
 */
public class CheckIdempotentCreate {

    public void service(ServerHttpConnection connection, IdempotencyRegistry registry,
            OwnerRepository ownerRepository, OwnerMapper ownerMapper) throws IdempotentReplayException {
        String key = idempotencyKey(connection);
        Integer existingId = registry.find(key);
        if (existingId == null) {
            return; // no key, or key not seen before: create as normal
        }
        Owner existing = ownerRepository.findById(existingId);
        if (existing == null) {
            return; // recorded owner is gone: fall through and create afresh
        }
        throw new IdempotentReplayException(ownerMapper.toOwnerDto(existing));
    }

    /** The trimmed {@code Idempotency-Key} header value, or {@code null} when absent or blank. */
    static String idempotencyKey(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
