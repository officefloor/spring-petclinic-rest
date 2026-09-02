package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalises the create request address in place into one canonical form: trimmed, internal
 * whitespace collapsed, upper-cased, and common street-type abbreviations expanded (ST-&gt;STREET,
 * RD-&gt;ROAD, AVE-&gt;AVENUE). Mutating the published {@link OwnerFieldsDto} means {@link BuildOwner}
 * stores the normalised value and every later comparison — household duplicate detection and the
 * shared household id — sees it. A null address is left for {@code RequireOwnerFields} to reject.
 */
public class NormalizeOwnerAddress {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    public void service(@Val OwnerFieldsDto request) {
        String address = request.getAddress();
        if (address == null) {
            return;
        }
        StringBuilder normalized = new StringBuilder();
        for (String token : address.trim().toUpperCase().split("\\s+")) {
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        request.setAddress(normalized.toString());
    }
}
