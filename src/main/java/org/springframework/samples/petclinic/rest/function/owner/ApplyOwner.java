package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        // Prefer the structured address fields when present, composing the flat 'address' from the
        // normalized addressLine1 (plus a space and normalized addressLine2 when supplied); otherwise
        // fall back to the flat 'address' input.
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        if (!line1.isEmpty()) {
            String line2 = AddressNormalizer.normalize(request.getAddressLine2());
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2.isEmpty() ? null : line2);
            owner.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
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
        owner.setPostcode(request.getPostcode());
    }
}
