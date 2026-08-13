package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Builds the {@link Owner} entity from the request body published by
 * {@link ValidateOwnerFields} (which is the single {@code @RequestBody} reader in the
 * {@code POST /api/owners} pipeline).
 */
public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        built.set(ownerMapper.toOwner(request));
    }
}
