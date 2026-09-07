package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request)
            throws InvalidEmailException, InvalidTelephoneException {
        // Normalize each supplied address part and store the composed 'address' (structured
        // addressLine1 preferred, flat 'address' as fallback), consistent with owner creation.
        String line1 = OwnerAddress.normalizeOrNull(request.getAddressLine1());
        String line2 = OwnerAddress.normalizeOrNull(request.getAddressLine2());
        owner.setAddressLine1(line1);
        owner.setAddressLine2(line2);
        owner.setAddress(OwnerAddress.compose(line1, line2, request.getAddress()));
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        owner.setEmail(OwnerEmail.normalize(request.getEmail()));
        owner.setPostcode(request.getPostcode());
    }
}
