package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.support.IdempotencyStore;

/**
 * First step of the {@code POST /api/owners} pipeline: enforces idempotent creates.
 *
 * <p>When the request carries an {@code Idempotency-Key} header whose value has already
 * created an owner (recorded by {@link RecordIdempotencyKey}), the originally created owner
 * is loaded and the {@code existing} branch is taken — {@link RespondWithExistingOwner}
 * returns it with 200 and the rest of the create pipeline is skipped, so no duplicate is
 * created. Otherwise the (possibly absent) key is published for {@link RecordIdempotencyKey}
 * to store once the create succeeds, and the pipeline continues to validation.
 *
 * <p>Reads the header directly off the connection rather than binding it, so a missing header
 * is simply {@code null} (not a 400). Does not bind {@code @RequestBody} — the body is read
 * once, later, by {@link ValidateOwnerFields}.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface ExistingOwnerFlow {
        void flow(Owner owner);
    }

    public void service(ServerHttpConnection connection, IdempotencyStore idempotencyStore,
            OwnerRepository ownerRepository, Out<String> idempotencyKey,
            @Flow("existing") ExistingOwnerFlow existing) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = header == null ? null : header.getValue();
        idempotencyKey.set(key);
        if (key == null || key.isBlank()) {
            return; // no key: always a fresh create
        }
        Integer existingId = idempotencyStore.find(key);
        if (existingId == null) {
            return; // first time this key is seen: create as normal
        }
        Owner owner = findExisting(ownerRepository, existingId);
        if (owner != null && !owner.isDeleted()) {
            existing.flow(owner); // replay: return the original owner (200), skip create
        }
    }

    private static Owner findExisting(OwnerRepository ownerRepository, int ownerId) {
        try {
            return ownerRepository.findById(ownerId);
        }
        catch (DataAccessException ex) {
            return null; // the recorded owner is gone: fall back to a fresh create
        }
    }
}
