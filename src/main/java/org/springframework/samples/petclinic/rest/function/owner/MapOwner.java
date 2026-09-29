package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Stamps the owner's household size via {@link OwnerHousehold} and maps it to its {@link OwnerDto}
 * once, publishing the DTO for downstream steps. Both {@link AuditOwnerCreated} and
 * {@link RespondWithOwnerCreated} read the published DTO rather than re-deriving it, so the audit
 * line and the response body are guaranteed to agree and the derivation lives in a single place.
 */
public class MapOwner {

    public void service(@Val Owner owner, OwnerHousehold ownerHousehold, OwnerMapper ownerMapper,
            Out<OwnerDto> dtoOut) {
        dtoOut.set(ownerMapper.toOwnerDto(ownerHousehold.stamp(owner)));
    }
}
