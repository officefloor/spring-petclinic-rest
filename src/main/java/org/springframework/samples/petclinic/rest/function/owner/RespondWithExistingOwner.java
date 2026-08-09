package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Responds to an idempotent repeat create by returning the originally created owner with 200 (not
 * 201): the create did not happen again, so the result is a plain OK, not a fresh Created. Mirrors
 * {@link RespondWithOwner}'s body (including the bulk-signup warning) so a replay is indistinguishable
 * from reading the owner back. The owner arrives as {@code @Parameter} from the {@code replay} branch.
 */
public class RespondWithExistingOwner {

    public void service(@Parameter Owner owner, OwnerMapper ownerMapper,
            OwnerRepository ownerRepository, ObjectResponse<OwnerDto> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(OwnerBulkSignup.isWarning(ownerRepository, owner.getRegistrationDate()));
        dto.setCapacityWarning(OwnerCityCapacity.isWarning(ownerRepository, owner.getCity()));
        dto.setRiskFlag(OwnerRiskFlag.isRisk(owner, ownerRepository));
        response.send(dto);
    }
}
