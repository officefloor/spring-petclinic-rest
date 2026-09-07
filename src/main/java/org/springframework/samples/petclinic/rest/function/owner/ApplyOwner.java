package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        applyAddress(owner, request);
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        String email = request.getEmail();
        owner.setEmail(email == null || email.isBlank() ? null
                : email.trim().toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * Copy the request's address fields onto the owner, normalizing and preferring the structured
     * form: the normalized {@code addressLine1}/{@code addressLine2} are stored (or {@code null}
     * when blank) and {@code address} becomes the composed, normalized string (structured when
     * present, else the normalized flat address).
     */
    private static void applyAddress(Owner owner, OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        owner.setAddressLine1(line1.isEmpty() ? null : line1);
        owner.setAddressLine2(line2.isEmpty() ? null : line2);
        owner.setAddress(AddressNormalizer.compose(line1, line2, request.getAddress()));
        owner.setCity(request.getCity());
        owner.setPostcode(request.getPostcode());
    }
}
