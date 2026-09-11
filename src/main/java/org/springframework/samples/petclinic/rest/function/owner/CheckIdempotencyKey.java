package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * First step of the create pipeline: short-circuits a repeated create.
 *
 * <p>When the request carries an {@code Idempotency-Key} header that has already produced
 * an owner (recorded by {@link RecordIdempotencyKey} in {@link IdempotencyStore}), the
 * originally created owner is re-read and returned with 200 — the create functions after
 * this one never run, so no duplicate is made (which would otherwise be a 409 from
 * {@link RequireUniqueIdentity}).
 *
 * <p>Otherwise — no key, or a key not seen before — it takes the {@code proceed} branch to
 * run the normal create pipeline. The header (when present) is read again by
 * {@link RecordIdempotencyKey} after the owner is saved.
 */
public class CheckIdempotencyKey {

    /** The {@code proceed} output: run the normal create pipeline. */
    @FunctionalInterface
    public interface ProceedFlow {
        void proceed();
    }

    public void service(
            @RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore store, OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            @Flow("proceed") ProceedFlow proceed,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        if (key != null && !key.isBlank()) {
            Integer existingId = store.find(key);
            if (existingId != null) {
                Owner owner = ownerRepository.findById(existingId);
                response.send(ResponseEntity.ok(ownerMapper.toOwnerDto(owner)));
                return; // short-circuit: return the originally created owner with 200
            }
        }
        proceed.proceed();
    }
}
