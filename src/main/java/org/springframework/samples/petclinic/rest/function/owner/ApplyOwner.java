package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        String line1 = request.getAddressLine1();
        String line2 = request.getAddressLine2();
        owner.setAddressLine1(line1);
        owner.setAddressLine2(line2);
        // Prefer the structured fields when present; the flat address stays accepted for backward
        // compatibility. The update pipeline does not normalize, so the stored address is composed
        // from the raw lines (unlike the create path, which composes already-normalized lines).
        if (AddressNormalizer.hasStructuredAddress(line1)) {
            owner.setAddress(AddressNormalizer.compose(line1, line2));
        }
        else {
            owner.setAddress(request.getAddress());
        }
        owner.setCity(request.getCity());
        owner.setPostcode(request.getPostcode());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        owner.setEmail(request.getEmail());
    }
}
