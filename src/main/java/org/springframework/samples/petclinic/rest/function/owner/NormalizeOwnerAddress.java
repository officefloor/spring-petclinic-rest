package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.BlankAddressException;

/**
 * Normalizes the owner's address on create. Two forms are accepted: the structured fields
 * ({@code addressLine1} plus optional {@code addressLine2}) and, for backward compatibility,
 * the flat {@code address}. The structured form is PREFERRED — when {@code addressLine1} is
 * non-blank the flat input is ignored and the address is rebuilt from the structured lines.
 *
 * <p>Normalization applies to whichever fields are supplied: each value is trimmed, has its
 * whitespace collapsed, is upper-cased, and has the common street-type abbreviations expanded
 * (see {@link AddressNormalizer}). For the structured form the returned {@code address} is the
 * composed value — the normalized {@code addressLine1}, with a single space and the normalized
 * {@code addressLine2} appended when {@code addressLine2} is present. The normalized values are
 * stored back on the request so later steps build, compare, save and return them.
 *
 * <p>The required-address check runs against the normalized form and accepts EITHER form: an
 * owner with neither a non-blank {@code addressLine1} nor a non-blank flat {@code address} is
 * rejected 400 via {@link BlankAddressException}.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) throws BlankAddressException {
        // Normalize each supplied field, then apply the shared "prefer structured" rule.
        String normalizedLine1 = AddressNormalizer.normalize(request.getAddressLine1());
        String normalizedLine2 = AddressNormalizer.normalize(request.getAddressLine2());
        if (!normalizedLine1.isEmpty()) {
            request.setAddressLine1(normalizedLine1);
            request.setAddressLine2(normalizedLine2.isEmpty() ? null : normalizedLine2);
        }
        String normalizedFlat = AddressNormalizer.normalize(request.getAddress());
        String effective = AddressNormalizer.effectiveAddress(normalizedLine1, normalizedLine2, normalizedFlat);
        if (effective.isEmpty()) {
            throw new BlankAddressException(request.getAddress());
        }
        request.setAddress(effective);
    }
}
