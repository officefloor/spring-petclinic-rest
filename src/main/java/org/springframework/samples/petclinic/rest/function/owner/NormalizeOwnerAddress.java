package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;

/**
 * Normalizes the owner's address on create, mutating the validated request in place so later
 * steps persist and return the normalized value. The address is trimmed with internal whitespace
 * runs collapsed to a single space, upper-cased, and common street-type abbreviations are expanded
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). Because address is a required
 * field, an address that is blank after normalization is rejected 400 via
 * {@link InvalidAddressException} — the required-field check now applies to the normalized form, so
 * a whitespace-only value that passes the raw length constraint is still rejected here.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) throws InvalidAddressException {
        String normalized = normalize(request.getAddress());
        if (normalized.isEmpty()) {
            throw new InvalidAddressException("Address must not be blank after normalization");
        }
        request.setAddress(normalized);
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
