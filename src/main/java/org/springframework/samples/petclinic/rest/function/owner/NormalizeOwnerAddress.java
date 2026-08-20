package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the create-owner address: trims and collapses whitespace, upper-cases and expands
 * common street-type abbreviations ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
 * Mutates the published request in place so later steps store and return the normalized value, and
 * so everything derived from the address compares the normalized form.
 *
 * <p>The structured fields are preferred when present: a non-blank {@code addressLine1} drives a
 * composed {@code address} equal to the normalized line 1, with a single space and the normalized
 * {@code addressLine2} appended when a line 2 is present. Otherwise the flat {@code address} input is
 * normalized as before, keeping earlier flat-address requests working.
 *
 * <p>{@link ValidateOwnerFields} has already rejected an address that is blank in both forms, so at
 * this point the composed value normalizes to a non-empty string.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        if (!line1.isEmpty()) {
            request.setAddressLine1(line1);
            String line2 = AddressNormalizer.normalize(request.getAddressLine2());
            if (!line2.isEmpty()) {
                request.setAddressLine2(line2);
                request.setAddress(line1 + " " + line2);
            }
            else {
                request.setAddress(line1);
            }
        }
        else {
            request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        }
    }
}
