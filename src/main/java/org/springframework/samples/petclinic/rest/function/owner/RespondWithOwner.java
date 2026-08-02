package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwner {

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository,
            ObjectResponse<OwnerDto> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        java.util.Collection<Owner> allOwners = ownerRepository.findAll();
        dto.setMembershipNumber(OwnerMapper.membershipNumber(owner, allOwners));
        dto.setCustomerCode(OwnerMapper.customerCode(owner, allOwners));
        dto.setMembershipTier(OwnerMapper.membershipTier(owner, allOwners));
        dto.setNamesakeCount(OwnerMapper.namesakeCount(owner, allOwners));
        dto.setSharesHousehold(OwnerMapper.sharesHousehold(owner, allOwners));
        dto.setLocality(OwnerMapper.locality(owner, allOwners));
        response.send(dto);
    }
}
