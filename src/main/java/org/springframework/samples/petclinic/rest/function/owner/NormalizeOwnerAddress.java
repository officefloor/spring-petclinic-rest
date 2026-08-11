package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the create-owner address into its canonical form (see {@link AddressNormalizer}) and
 * writes it back onto the body so {@link BuildOwner} maps and {@link SaveOwner} stores the
 * normalized value, and {@code GET} returns it. Running before {@link AssignHousehold} means every
 * later step sees — and therefore compares and hashes — the normalized address.
 *
 * <p>The structured form is preferred: when a non-blank {@code addressLine1} is supplied, both
 * lines are normalized and the flat {@code address} is set to the composed value (the normalized
 * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended when
 * an {@code addressLine2} is present). Without a structured line the flat {@code address} is
 * normalized on its own, keeping the older payload shape working unchanged.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which rejects a request with an address blank in both
 * forms after normalization) and mutates the validated body in place — {@code @Val} yields the same
 * object the earlier step published.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        if (!line1.isEmpty()) {
            String line2 = AddressNormalizer.normalize(request.getAddressLine2());
            request.setAddressLine1(line1);
            request.setAddressLine2(line2.isEmpty() ? null : line2);
            request.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
        }
        else {
            request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        }
    }
}
