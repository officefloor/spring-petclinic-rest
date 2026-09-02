package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Rewrites the built owner's address into a canonical form in place, so later steps save, compare
 * (household duplicate detection and the shared household id) and return it as {@code address}. The
 * value is trimmed, its internal whitespace collapsed, upper-cased and common street-type
 * abbreviations expanded (ST->STREET, RD->ROAD, AVE->AVENUE). An address that is blank after
 * normalization is rejected 400 via {@link MissingOwnerFieldsException}.
 */
public class NormalizeOwnerAddress {

    private static final Map<String, String> ABBREVIATIONS = Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    public void service(@Val Owner owner) throws MissingOwnerFieldsException {
        String line1 = normalize(owner.getAddressLine1());
        if (!line1.isEmpty()) {
            String line2 = normalize(owner.getAddressLine2());
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2.isEmpty() ? null : line2);
            owner.setAddress(line2.isEmpty() ? line1 : line1 + ' ' + line2);
            return;
        }
        String address = normalize(owner.getAddress());
        if (address.isEmpty()) {
            throw new MissingOwnerFieldsException(List.of("address"));
        }
        owner.setAddress(address);
    }

    private static String normalize(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        StringBuilder normalized = new StringBuilder();
        for (String token : trimmed.split("\\s+")) {
            String upper = token.toUpperCase(Locale.ROOT);
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(upper, upper));
        }
        return normalized.toString();
    }
}
