package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;

/**
 * Normalizes the owner's address on create, mutating the validated request in place so later
 * steps persist and return the normalized value. Normalization (trim, collapse internal whitespace
 * to a single space, upper-case, expand common street-type abbreviations {@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}) applies to whichever address fields are supplied.
 *
 * <p>The structured form is preferred: when a non-blank {@code addressLine1} is supplied it (and the
 * optional {@code addressLine2}) are normalized in place, and the flat {@code address} is set to the
 * composed value — the normalized {@code addressLine1}, plus a single space and the normalized
 * {@code addressLine2} when {@code addressLine2} is present. Otherwise the flat {@code address} is
 * normalized directly. Either way, an address that is blank after normalization is rejected 400 via
 * {@link InvalidAddressException}, so a whitespace-only value that passes the raw length constraint
 * is still rejected here.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) throws InvalidAddressException {
        if (OwnerAddresses.hasStructured(request)) {
            String normalizedLine1 = normalizeRequired(request.getAddressLine1());
            request.setAddressLine1(normalizedLine1);
            String normalizedLine2 = null;
            String line2 = request.getAddressLine2();
            if (line2 != null && !line2.isBlank()) {
                normalizedLine2 = normalize(line2);
                request.setAddressLine2(normalizedLine2);
            }
            request.setAddress(OwnerAddresses.compose(normalizedLine1, normalizedLine2));
        }
        else {
            request.setAddress(normalizeRequired(request.getAddress()));
        }
    }

    /**
     * Normalizes a supplied address value and rejects it 400 when it is blank after normalization,
     * so a whitespace-only value that passes the raw length constraint is still rejected here.
     */
    private static String normalizeRequired(String value) throws InvalidAddressException {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            throw new InvalidAddressException("Address must not be blank after normalization");
        }
        return normalized;
    }

    /**
     * The single source of truth for the normalized form of an address: trimmed with internal
     * whitespace collapsed, upper-cased, and common street-type abbreviations expanded. Returns the
     * empty string for a null or blank input. Shared with {@link AssignOwnerHousehold} so the request
     * side and the shared household id are compared on identical normalized text.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(expand(tokens[i]));
        }
        return sb.toString();
    }

    private static String expand(String token) {
        switch (token) {
            case "ST":
                return "STREET";
            case "RD":
                return "ROAD";
            case "AVE":
                return "AVENUE";
            default:
                return token;
        }
    }
}
