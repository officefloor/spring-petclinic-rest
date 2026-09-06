package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) throws InvalidPostcodeException {
        // Postcode is optional; when present it must be valid for the city's region.
        Postcodes.validate(request.getCity(), request.getPostcode());
        // Prefer the structured address fields when present, falling back to the flat 'address';
        // both the individual lines and the composed 'address' are stored normalized.
        String addressLine1 = Addresses.normalizeToNull(request.getAddressLine1());
        String addressLine2 = Addresses.normalizeToNull(request.getAddressLine2());
        owner.setAddressLine1(addressLine1);
        owner.setAddressLine2(addressLine2);
        owner.setAddress(Addresses.compose(addressLine1, addressLine2, request.getAddress()));
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        owner.setEmail(request.getEmail());
        owner.setPostcode(request.getPostcode());
    }
}
