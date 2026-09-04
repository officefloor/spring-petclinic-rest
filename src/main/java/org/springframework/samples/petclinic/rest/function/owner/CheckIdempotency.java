package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * First step of the create pipeline: when the request repeats an already-seen
 * {@code Idempotency-Key}, respond 200 with the originally created owner and skip the
 * create; otherwise proceed with the normal pipeline.
 */
public class CheckIdempotency {

    @FunctionalInterface
    public interface Proceed {
        void run();
    }

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore store, OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            ObjectResponse<OwnerDto> response, @Flow("proceed") Proceed proceed) {
        Integer ownerId = key == null ? null : store.find(key);
        if (ownerId == null) {
            proceed.run();
            return;
        }
        Owner owner = ownerRepository.findById(ownerId);
        response.send(ownerMapper.toOwnerDto(owner)); // 200 by default
    }
}
