package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwner {

    /** Email-domain families whose presence marks a domain as disposable-adjacent. */
    private static final Set<String> DISPOSABLE_BASES =
            Set.of("mailinator", "tempmail", "guerrillamail");

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository,
            ObjectResponse<OwnerDto> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(BulkSignup.warns(ownerRepository));
        dto.setCapacityWarning(CityCapacity.approaching(owner, ownerRepository));
        dto.setRiskFlag(riskFlag(owner, ownerRepository));
        dto.setSelfLink("/api/owners/" + owner.getId());
        response.send(dto);
    }

    private static boolean riskFlag(Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || CityCapacity.approaching(owner, ownerRepository)) {
            return true;
        }
        String email = owner.getEmail();
        if (email == null || email.indexOf('@') < 0) {
            return false;
        }
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        return DISPOSABLE_BASES.stream().anyMatch(domain::contains);
    }
}
