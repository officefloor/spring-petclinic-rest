package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Email is optional. When absent (null or blank) the owner is left unchanged. When present it
 * must be a syntactically valid address; the validated {@link OwnerFieldsDto} is mutated in place
 * so the stored and returned value is lower-cased, or {@link InvalidEmailException} is thrown for a
 * 400 when the address is not syntactically valid.
 */
public class NormalizeOwnerEmail {

    /**
     * Single-{@code @} address with non-empty, whitespace-free local and domain parts and at least
     * one dot in the domain (e.g. {@code user@example.com}). Sufficient to reject values such as
     * {@code not-an-email} while accepting ordinary addresses.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(trimmed.toLowerCase(Locale.ROOT));
    }
}
