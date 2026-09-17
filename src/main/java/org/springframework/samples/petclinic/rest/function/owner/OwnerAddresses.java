package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared address canonicalization for the owner pipelines: reduces an address to a single
 * normalized form so the required-field check, household comparisons and storage all use
 * the same value.
 *
 * <p>Normalization trims surrounding whitespace, collapses each internal run of whitespace
 * to a single space, upper-cases, and expands common street-type abbreviations
 * ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}) as whole words. It is
 * idempotent: normalizing an already-normalized address returns it unchanged.
 *
 * <p>A request may carry the address in a structured form ({@code addressLine1} plus an
 * optional {@code addressLine2}) or in the legacy flat {@code address} field. The structured
 * form is preferred when {@code addressLine1} is supplied; otherwise the flat field is used.
 * Either way {@link #normalize(OwnerFieldsDto)} leaves the request with a normalized
 * {@code address} that the rest of the pipeline reads.
 */
final class OwnerAddresses {

    /** Whole-word abbreviation expansions, keyed by their upper-cased form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddresses() {
    }

    /**
     * Canonical form of {@code address}: trimmed, internal whitespace collapsed to single
     * spaces, upper-cased, with each abbreviation token expanded. A {@code null} address, or
     * one that is empty once trimmed, normalizes to {@code ""} (which the required-field check
     * then rejects as blank).
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (String token : collapsed.split(" ")) {
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return normalized.toString();
    }

    /**
     * Normalizes the request's address in place. When the structured form is supplied (a
     * non-blank {@code addressLine1}), each supplied line is normalized and the flat
     * {@code address} is set to the composed value; otherwise the flat {@code address} is
     * normalized on its own, preserving the legacy behaviour. After this call the request's
     * {@code address} holds the value the required-field check, household comparisons, storage
     * and the response all use.
     */
    static void normalize(OwnerFieldsDto request) {
        if (isBlank(request.getAddressLine1())) {
            request.setAddress(normalize(request.getAddress()));
            return;
        }
        String line1 = normalize(request.getAddressLine1());
        request.setAddressLine1(line1);
        String line2 = isBlank(request.getAddressLine2()) ? null : normalize(request.getAddressLine2());
        request.setAddressLine2(line2);
        request.setAddress(compose(line1, line2));
    }

    /**
     * Composes the flat address from the normalized structured lines: {@code line1}, with a
     * single space and {@code line2} appended when {@code line2} is present.
     */
    static String compose(String line1, String line2) {
        return line2 == null ? line1 : line1 + " " + line2;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
