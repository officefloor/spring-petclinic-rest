package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        // Prefer the structured address fields when present, falling back to the flat 'address'. Both
        // are normalized, and the stored/returned 'address' is the composed effective value (see
        // OwnerAddress), mirroring the create path so an update never leaves an un-normalized address.
        owner.setAddressLine1(OwnerAddress.normalizeToNull(request.getAddressLine1()));
        owner.setAddressLine2(OwnerAddress.normalizeToNull(request.getAddressLine2()));
        owner.setAddress(OwnerAddress.effective(
                request.getAddressLine1(), request.getAddressLine2(), request.getAddress()));
        owner.setCity(request.getCity());
        owner.setPostcode(request.getPostcode());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        owner.setEmail(request.getEmail());
    }
}
