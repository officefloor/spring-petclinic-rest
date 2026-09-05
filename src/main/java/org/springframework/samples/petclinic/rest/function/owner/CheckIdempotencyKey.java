package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * First step of the create-owner pipeline. If the request carries an {@code Idempotency-Key} header
 * whose value already created an owner, responds with that originally created owner and {@code 200 OK}
 * instead of creating a duplicate. Otherwise takes the {@code proceed} branch to run the normal
 * create pipeline (which reads the request body). A missing or blank header always proceeds — the
 * header resolves to {@code null} when absent, so key-less creates are unaffected.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface Proceed {
        void proceed();
    }

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            IdempotencyStore store, OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            @Flow("proceed") Proceed proceed, ObjectResponse<OwnerDto> response) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer existingId = store.find(idempotencyKey);
            if (existingId != null) {
                Owner existing = ownerRepository.findById(existingId);
                if (existing != null) {
                    response.send(ownerMapper.toOwnerDto(existing));
                    return; // idempotent replay: original owner, 200 OK
                }
            }
        }
        proceed.proceed();
    }
}
