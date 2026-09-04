package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rewrites the create body's address into a canonical form: trimmed with collapsed
 * whitespace, upper-cased, and common street-type abbreviations expanded (ST-&gt;STREET,
 * RD-&gt;ROAD, AVE-&gt;AVENUE). Runs before {@link BuildOwner}, so the persisted and
 * returned value is already normalized and every later address comparison (household
 * duplicate detection and the shared household id) sees the same normalized string.
 * {@link RequireOwnerFields} runs first and rejects a whitespace-only address, so the
 * value here is never blank.
 */
public class NormalizeOwnerAddress {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    public void service(@Val OwnerFieldsDto request) {
        String[] words = request.getAddress().trim().toUpperCase().split("\\s+");
        StringBuilder normalized = new StringBuilder();
        for (String word : words) {
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(word, word));
        }
        request.setAddress(normalized.toString());
    }
}
