package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;

/**
 * On create, canonicalizes the owner's address. Structured {@code addressLine1}/{@code addressLine2}
 * are preferred when a non-blank {@code addressLine1} is supplied; otherwise the flat {@code address}
 * is used. Each supplied part is trimmed, whitespace-collapsed, upper-cased and has common
 * abbreviations expanded (ST-&gt;STREET, RD-&gt;ROAD, AVE-&gt;AVENUE). The stored address is the normalized
 * {@code addressLine1} with a single space and the normalized {@code addressLine2} appended when
 * {@code addressLine2} is present. An owner with neither form of address is rejected with a 400.
 */
public class NormalizeOwnerAddress {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) throws InvalidAddressException {
        String line1 = normalize(request.getAddressLine1());
        if (!line1.isEmpty()) {
            String line2 = normalize(request.getAddressLine2());
            owner.setAddressLine1(line1);
            owner.setAddress(line2.isEmpty() ? line1 : line1 + ' ' + line2);
            return;
        }
        String flat = normalize(owner.getAddress());
        if (flat.isEmpty()) {
            throw new InvalidAddressException("An owner must have an address");
        }
        owner.setAddress(flat);
    }

    private static String normalize(String raw) {
        StringBuilder normalized = new StringBuilder();
        for (String token : (raw == null ? "" : raw).trim().split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            String upper = token.toUpperCase();
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(upper, upper));
        }
        return normalized.toString();
    }
}
