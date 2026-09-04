package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flat = AddressNormalizer.normalize(request.getAddress());

        // Prefer the structured addressLine1; fall back to the flat address.
        String effectiveLine1 = !line1.isEmpty() ? line1 : flat;
        String composed = line2.isEmpty() ? effectiveLine1 : effectiveLine1 + " " + line2;

        owner.setAddressLine1(effectiveLine1.isEmpty() ? null : effectiveLine1);
        owner.setAddressLine2(line2.isEmpty() ? null : line2);
        owner.setAddress(composed);
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        owner.setEmail(request.getEmail());
        owner.setPostcode(request.getPostcode());
    }
}
