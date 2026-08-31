package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * On create, rewrites the address into a canonical form: whitespace is trimmed and
 * collapsed to single spaces, letters are upper-cased, and common abbreviations are
 * expanded (ST-&gt;STREET, RD-&gt;ROAD, AVE-&gt;AVENUE). Mutates the built {@link Owner} in
 * place so later steps store, compare and return the normalized value. A blank input is
 * already rejected as a required field before this step runs.
 */
public class NormalizeOwnerAddress {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    public void service(@Val Owner owner) {
        String raw = owner.getAddress() == null ? "" : owner.getAddress();
        StringBuilder normalized = new StringBuilder();
        for (String token : raw.trim().split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            String upper = token.toUpperCase();
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(upper, upper));
        }
        owner.setAddress(normalized.toString());
    }
}
