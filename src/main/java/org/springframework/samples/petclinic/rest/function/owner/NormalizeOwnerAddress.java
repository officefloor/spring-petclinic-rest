package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.util.StringUtils;

/**
 * Normalizes an owner request's address in place into canonical form (see {@link AddressNormalizer}):
 * trimmed, whitespace-collapsed, upper-cased, with common street-type abbreviations expanded.
 * Normalization applies to whichever fields are supplied — the structured {@code addressLine1} /
 * {@code addressLine2} when present, otherwise the flat {@code address} — and the flat {@code address}
 * is set to the composed canonical value, so every later step (storage, household identity) reads one
 * normalized address regardless of the form it arrived in.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        if (StringUtils.hasText(request.getAddressLine1())) {
            request.setAddressLine1(AddressNormalizer.normalize(request.getAddressLine1()));
            request.setAddressLine2(StringUtils.hasText(request.getAddressLine2())
                    ? AddressNormalizer.normalize(request.getAddressLine2()) : null);
        }
        request.setAddress(AddressNormalizer.compose(
                request.getAddressLine1(), request.getAddressLine2(), request.getAddress()));
    }
}
