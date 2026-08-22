package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        applyAddress(owner, request);
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        String email = request.getEmail();
        owner.setEmail(email == null || email.isBlank() ? null : email.trim().toLowerCase());
    }

    /**
     * Copies the address, preferring the structured form. A non-blank {@code addressLine1} (with an
     * optional {@code addressLine2}) wins over the flat {@code address}; the stored {@code address}
     * is the composed value, falling back to the flat {@code address}.
     */
    private static void applyAddress(Owner owner, OwnerFieldsDto request) {
        String line1 = request.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            owner.setAddressLine1(line1);
            String line2 = request.getAddressLine2();
            if (line2 != null && !line2.isBlank()) {
                owner.setAddressLine2(line2);
                owner.setAddress(line1 + " " + line2);
            }
            else {
                owner.setAddressLine2(null);
                owner.setAddress(line1);
            }
        }
        else {
            owner.setAddress(request.getAddress());
        }
    }
}
