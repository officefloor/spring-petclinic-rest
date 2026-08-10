package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the optional owner email. Email may be absent: a null or blank value is left untouched
 * and passes through. When present it is trimmed and lower-cased, then required to be a syntactically
 * valid address; the normalized (lower-cased) value is written back onto the validated body (mutated in
 * place via {@code @Val}) so downstream steps store and return it lower-cased. A present-but-invalid
 * address is rejected by throwing {@link InvalidEmailException} (handled as 400). An otherwise valid
 * address whose domain is on the disposable-domain blocklist is likewise rejected as 400.
 */
public class NormalizeOwnerEmail {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** Disposable email domains that are not accepted for owner registration. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalized).matches()) {
            throw new InvalidEmailException(email);
        }
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(normalized);
    }
}
