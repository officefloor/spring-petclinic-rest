package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;

/**
 * Canonicalises the address whenever an owner is created: whitespace is trimmed and collapsed,
 * the text is upper-cased and common abbreviations are expanded ({@code ST}->{@code STREET},
 * {@code RD}->{@code ROAD}, {@code AVE}->{@code AVENUE}). The normalized value is stored back on
 * the request so it is persisted and returned as {@code address}, and so every downstream
 * comparison (household duplicate detection, the shared household id) sees the canonical form. An
 * address that is blank after normalization is rejected with 400 via {@link MissingFieldsException}.
 */
public class NormalizeAddress {

    public void service(@Val OwnerFieldsDto request) throws MissingFieldsException {
        String normalized = request.getAddress().trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT)
                .replaceAll("\\bST\\b", "STREET")
                .replaceAll("\\bRD\\b", "ROAD")
                .replaceAll("\\bAVE\\b", "AVENUE");
        if (normalized.isBlank()) {
            throw new MissingFieldsException(List.of("address"));
        }
        request.setAddress(normalized);
    }
}
