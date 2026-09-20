package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Entry point of the create pipeline. Reads the optional {@code Idempotency-Key} header: if
 * it names an owner already created under that key, that owner is republished and the
 * {@code replay} branch responds with it (a 200), so a repeated create is never duplicated.
 * Otherwise the key (which may be absent) is published for {@link RecordIdempotencyKey} and
 * the normal {@code proceed} branch runs. A stored key whose owner no longer exists is
 * treated as unseen, so the request proceeds normally.
 */
public class CheckIdempotencyKey {

    private static final String HEADER = "Idempotency-Key";

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            OwnerRepository ownerRepository, Out<Owner> owner, Out<IdempotencyKey> key,
            @Flow("replay") Runnable replay, @Flow("proceed") Runnable proceed) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        String value = header != null ? header.getValue() : null;
        if (value != null && !value.isBlank()) {
            Integer existingId = store.find(value);
            if (existingId != null) {
                Owner existing = ownerRepository.findById(existingId);
                if (existing != null) {
                    owner.set(existing);
                    replay.run();
                    return;
                }
            }
        }
        key.set(new IdempotencyKey(value));
        proceed.run();
    }
}
