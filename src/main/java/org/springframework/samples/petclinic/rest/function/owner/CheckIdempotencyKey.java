package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * First step of owner creation: when an {@code Idempotency-Key} header repeats a key already seen,
 * respond 200 with the originally created owner instead of creating a duplicate. Otherwise the
 * {@code proceed} flow continues on to build and save a new owner.
 */
public class CheckIdempotencyKey {

    /** Key seen on a create -> id of the owner it created. Records survive test rollback by design. */
    static final Map<String, Integer> SEEN = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface Proceed {
        void proceed();
    }

    /** The {@code Idempotency-Key} header value, or {@code null} when the header is absent. */
    static String key(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        return header == null ? null : header.getValue();
    }

    public void service(ServerHttpConnection connection, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<ResponseEntity<OwnerDto>> response,
            @Flow("proceed") Proceed proceed) {
        String key = key(connection);
        Integer ownerId = key == null ? null : SEEN.get(key);
        Owner owner = ownerId == null ? null : ownerRepository.findById(ownerId);
        if (owner == null) {
            proceed.proceed();
            return;
        }
        response.send(ResponseEntity.ok(ownerMapper.toOwnerDto(owner)));
    }
}
