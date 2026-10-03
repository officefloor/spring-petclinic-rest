package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;

/**
 * Normalizes the submitted address on create via {@link OwnerAddresses} (trim and collapse
 * whitespace, upper-case, expand common abbreviations). Two input forms are accepted: the
 * STRUCTURED form ({@code addressLine1} plus an optional {@code addressLine2}) and, for
 * backward compatibility, the FLAT {@code address} form. The structured form is preferred
 * whenever {@code addressLine1} is supplied (non-blank).
 *
 * <p>Normalization applies to whichever address fields are supplied, and the normalized
 * values are written back onto the request in place (the {@code @Val} object is shared with
 * later steps). In the structured form the composed {@code address} is set to the normalized
 * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended
 * when {@code addressLine2} is present; everything downstream that reads {@code address} then
 * sees the structured value, falling back to the flat address when no structured fields are
 * given. An owner that supplies no address in either form — no non-blank {@code addressLine1}
 * and no non-blank flat {@code address} — is rejected with 400 via
 * {@link InvalidAddressException}.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) throws InvalidAddressException {
        String line1 = request.getAddressLine1();
        String address;
        if (line1 != null && !line1.isBlank()) {
            // Structured form: normalize line 1 and the optional line 2 in place (so the
            // response returns them normalized) and compose the address downstream reads.
            String normalizedLine1 = OwnerAddresses.normalize(line1);
            request.setAddressLine1(normalizedLine1);

            String line2 = request.getAddressLine2();
            String normalizedLine2 = null;
            if (line2 != null && !line2.isBlank()) {
                normalizedLine2 = OwnerAddresses.normalize(line2);
                request.setAddressLine2(normalizedLine2);
            }
            address = OwnerAddresses.compose(normalizedLine1, normalizedLine2);
        }
        else {
            // Flat form: normalize the supplied address.
            address = OwnerAddresses.normalize(request.getAddress());
        }

        // Valid only when an address was supplied in one of the two forms; a blank one
        // (nothing left after normalization) is a 400.
        if (address == null || address.isEmpty()) {
            throw new InvalidAddressException(request.getAddress());
        }
        request.setAddress(address);
    }
}
