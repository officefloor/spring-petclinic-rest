package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalises the create request address in place into one canonical form: trimmed, internal
 * whitespace collapsed, upper-cased, and common street-type abbreviations expanded (ST-&gt;STREET,
 * RD-&gt;ROAD, AVE-&gt;AVENUE). Applies to whichever fields are supplied: the structured
 * {@code addressLine1}/{@code addressLine2} are preferred and the flat {@code address} stays
 * accepted. The stored {@code address} is composed from the normalised line 1, with a single space
 * and the normalised line 2 appended when present, else it is the normalised flat address. Mutating
 * the published {@link OwnerFieldsDto} means {@link BuildOwner} stores the normalised values and
 * every later comparison sees them. A blank line is treated as absent.
 */
public class NormalizeOwnerAddress {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    public void service(@Val OwnerFieldsDto request) {
        String line1 = normalize(request.getAddressLine1());
        String line2 = normalize(request.getAddressLine2());
        request.setAddressLine1(line1);
        request.setAddressLine2(line2);
        if (line1 != null) {
            request.setAddress(line2 == null ? line1 : line1 + " " + line2);
        }
        else {
            request.setAddress(normalize(request.getAddress()));
        }
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        StringBuilder normalized = new StringBuilder();
        for (String token : value.trim().toUpperCase().split("\\s+")) {
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return normalized.toString();
    }
}
