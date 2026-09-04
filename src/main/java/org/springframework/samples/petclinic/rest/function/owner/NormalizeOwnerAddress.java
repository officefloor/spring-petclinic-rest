package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rewrites the create body's address into a canonical form: trimmed with collapsed
 * whitespace, upper-cased, and common street-type abbreviations expanded (ST-&gt;STREET,
 * RD-&gt;ROAD, AVE-&gt;AVENUE). The structured {@code addressLine1}/{@code addressLine2}
 * fields are preferred when supplied: each is normalized and {@code address} is set to
 * the composed line 1 (plus a single space and line 2 when present); otherwise the flat
 * {@code address} is normalized in place. Runs before {@link BuildOwner}, so the
 * persisted and returned values are already normalized. {@link RequireOwnerFields} runs
 * first and rejects a body with neither address form, so a value is always present here.
 */
public class NormalizeOwnerAddress {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    public void service(@Val OwnerFieldsDto request) {
        String line1 = normalize(request.getAddressLine1());
        if (line1 != null) {
            String line2 = normalize(request.getAddressLine2());
            request.setAddressLine1(line1);
            request.setAddressLine2(line2);
            request.setAddress(line2 == null ? line1 : line1 + " " + line2);
        } else {
            request.setAddress(normalize(request.getAddress()));
        }
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        StringBuilder normalized = new StringBuilder();
        for (String word : value.trim().toUpperCase().split("\\s+")) {
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(word, word));
        }
        return normalized.toString();
    }
}
