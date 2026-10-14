package org.springframework.samples.petclinic.rest.validation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.AddressNormalizer;

/**
 * Determines which of the fields required to create an owner are missing or blank. The address may be
 * supplied either as a structured 'addressLine1' or as the flat 'address'; it is checked in its
 * normalized form, so an address that is blank after normalization counts as missing.
 */
public final class RequiredOwnerFields {

    private RequiredOwnerFields() {
    }

    /**
     * @param owner the owner fields from the request body
     * @return names of required fields that are missing or blank, in declaration order; empty if none
     */
    public static List<String> missingFields(OwnerFieldsDto owner) {
        Map<String, String> required = new LinkedHashMap<>();
        required.put("firstName", owner.getFirstName());
        required.put("lastName", owner.getLastName());
        required.put("address", AddressNormalizer.compose(
            owner.getAddressLine1(), owner.getAddressLine2(), owner.getAddress()));
        required.put("city", owner.getCity());
        required.put("telephone", owner.getTelephone());
        List<String> missing = new ArrayList<>();
        required.forEach((field, value) -> {
            if (value == null || value.isBlank()) {
                missing.add(field);
            }
        });
        return missing;
    }
}
