package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * The single home for the owner address rules that would otherwise be restated in each function
 * class. {@link OwnerFieldsDto} is generated from {@code openapi.yml} and cannot host these, so the
 * two request-side decisions live here and are shared by {@link ValidateOwner},
 * {@link NormalizeOwnerAddress} and {@link ApplyOwner}:
 *
 * <ul>
 * <li>whether the request uses the structured address form (preferred when {@code addressLine1} is
 * non-blank), and</li>
 * <li>how the flat {@code address} is composed from the two structured lines.</li>
 * </ul>
 */
final class OwnerAddresses {

    private OwnerAddresses() {
    }

    /** The structured form is preferred whenever a non-blank {@code addressLine1} is supplied. */
    static boolean hasStructured(OwnerFieldsDto request) {
        String line1 = request.getAddressLine1();
        return line1 != null && !line1.isBlank();
    }

    /**
     * The composed flat address: {@code line1}, plus a single space and {@code line2} when
     * {@code line2} is present (non-blank). Callers pass either the normalized lines (create) or the
     * raw lines (update); the join rule itself lives only here.
     */
    static String compose(String line1, String line2) {
        return (line2 != null && !line2.isBlank()) ? line1 + " " + line2 : line1;
    }
}
