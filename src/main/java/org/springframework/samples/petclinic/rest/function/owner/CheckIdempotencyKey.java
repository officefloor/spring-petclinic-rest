package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * First step of the create-owner pipeline. When the request carries an {@code Idempotency-Key}
 * already seen by a previous successful create (see {@link IdempotencyStore}), the originally
 * created owner is republished and the {@code existing} branch responds 200 without creating a
 * duplicate. Otherwise the key is published for {@link RecordIdempotencyKey} to remember once
 * the owner is saved, and the ordinary {@code create} branch runs. An absent key always takes
 * the create branch.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface Existing {
        void branch();
    }

    @FunctionalInterface
    public interface Create {
        void branch();
    }

    public void service(ServerHttpConnection connection,
            IdempotencyStore idempotencyStore, OwnerRepository ownerRepository,
            Out<String> idempotencyKey, Out<Owner> existingOwner,
            @Flow("existing") Existing existing, @Flow("create") Create create) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = header == null ? null : header.getValue();
        Integer existingId = idempotencyStore.find(key);
        if (existingId != null) {
            Owner owner = ownerRepository.findById(existingId);
            if (owner != null) {
                existingOwner.set(owner);
                existing.branch();
                return;
            }
        }
        idempotencyKey.set(key == null ? "" : key);
        create.branch();
    }
}
