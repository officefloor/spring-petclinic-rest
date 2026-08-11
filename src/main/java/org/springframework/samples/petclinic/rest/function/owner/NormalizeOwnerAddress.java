package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the create-owner {@code address} into its canonical form (see
 * {@link AddressNormalizer}) and writes it back onto the body so {@link BuildOwner} maps and
 * {@link SaveOwner} stores the normalized value, and {@code GET} returns it. Running before
 * {@link CheckOwnerHouseholdUnique} and {@link AssignHousehold} means those steps see — and
 * therefore compare and hash — the normalized address.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which rejects an address blank after
 * normalization) and mutates the validated body in place — {@code @Val} yields the same object
 * the earlier step published.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
    }
}
