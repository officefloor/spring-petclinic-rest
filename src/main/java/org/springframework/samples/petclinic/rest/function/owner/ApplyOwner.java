package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        String line1 = NormalizeOwnerAddress.normalize(request.getAddressLine1());
        if (!line1.isEmpty()) {
            // Structured address supplied: prefer it and compose the stored flat address.
            String line2 = NormalizeOwnerAddress.normalize(request.getAddressLine2());
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2.isEmpty() ? null : line2);
            owner.setAddress(NormalizeOwnerAddress.compose(line1, line2));
        }
        else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(request.getAddress());
        }
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        owner.setEmail(request.getEmail());
    }
}
