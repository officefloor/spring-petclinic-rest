package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * First step of {@code POST /api/owners}. If the request carries an {@code Idempotency-Key}
 * header whose value has already created an owner, this short-circuits the whole create
 * pipeline and re-sends that original owner with 200 (rather than creating a duplicate, which
 * a repeat of the same identity would otherwise reject with 409). Absent an already-seen key
 * it takes the {@code proceed} branch and the normal create pipeline runs; the key is recorded
 * once the owner is saved by {@link RecordOwnerIdempotency}.
 */
public class CheckOwnerIdempotency {

    @FunctionalInterface
    public interface ProceedFlow {
        void proceed();
    }

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            OwnerIdempotencyStore idempotencyStore, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, @Flow("proceed") ProceedFlow proceed,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        Integer existingId = idempotencyStore.find(idempotencyKey);
        if (existingId != null) {
            Owner owner = ownerRepository.findById(existingId);
            response.send(ResponseEntity.ok(ownerMapper.toOwnerDto(owner)));
            return; // key already seen: re-send the original owner, do not create again
        }
        proceed.proceed();
    }
}
