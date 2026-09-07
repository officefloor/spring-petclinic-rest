package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * First step of {@code POST /api/owners}. Honours an optional {@code Idempotency-Key} header: when
 * the request repeats a key already recorded by {@link RecordIdempotency}, the originally created
 * owner is returned with 200 and the create pipeline is short-circuited, so no duplicate is created
 * (and the usual identity-conflict 409 is never reached). Otherwise — no key, or a key not seen
 * before — the {@code create} flow is taken to run the normal validate/build/save pipeline.
 */
public class CheckIdempotency {

    /** Header naming the idempotency key; absent means a normal (non-idempotent) create. */
    static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    @FunctionalInterface
    public interface CreateFlow {
        void create();
    }

    public void service(ServerHttpConnection connection, IdempotencyStore idempotencyStore,
            OwnerRepository ownerRepository, OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response,
            @Flow("create") CreateFlow createFlow) {
        String key = idempotencyKey(connection);
        if (key != null) {
            Integer existingId = idempotencyStore.get(key);
            if (existingId != null) {
                Owner existing = ownerRepository.findById(existingId);
                if (existing != null) {
                    response.send(ownerMapper.toOwnerDto(existing)); // 200, no duplicate created
                    return; // short-circuit: do not take the create flow
                }
            }
        }
        createFlow.create();
    }

    /** The trimmed {@code Idempotency-Key} header value, or {@code null} when absent or blank. */
    static String idempotencyKey(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(IDEMPOTENCY_KEY_HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
