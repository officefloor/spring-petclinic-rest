package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Builds the {@link Owner} entity from the request body published by
 * {@link ValidateOwnerFields} (which is the single {@code @RequestBody} reader in the
 * {@code POST /api/owners} pipeline).
 *
 * <p>When the request omits a registration date it defaults to the server's current
 * date, so every created owner is persisted (and returned) with a registration date.
 */
public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        built.set(owner);
    }
}
