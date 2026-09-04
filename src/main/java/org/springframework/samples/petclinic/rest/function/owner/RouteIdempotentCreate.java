package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Entry step: routes an incoming create by its optional {@code Idempotency-Key}. A key
 * already seen (and whose owner still exists) branches to {@code existing} to return that
 * owner with 200; otherwise the key is published for later recording and {@code create}
 * runs the normal pipeline.
 */
public class RouteIdempotentCreate {

    @FunctionalInterface
    public interface Create {
        void proceed();
    }

    @FunctionalInterface
    public interface Existing {
        void repeat();
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            OwnerRepository ownerRepository, Out<String> keyOut, Out<Owner> ownerOut,
            @Flow("create") Create create, @Flow("existing") Existing existing) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = header == null ? null : header.getValue();
        if (key != null) {
            Integer ownerId = store.find(key);
            if (ownerId != null) {
                Owner owner = ownerRepository.findById(ownerId);
                if (owner != null) {
                    ownerOut.set(owner);
                    existing.repeat();
                    return;
                }
            }
        }
        keyOut.set(key == null ? "" : key);
        create.proceed();
    }
}
