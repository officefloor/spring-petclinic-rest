package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;

/**
 * Rejects a create request whose email domain is on the disposable-domain blocklist, so such an
 * address is a 400 via {@link MissingFieldsException}. Runs after {@link NormalizeEmail}, so the
 * email is already syntactically valid and lower-cased. An absent or blank email passes through.
 */
public class RejectDisposableEmail {

    private static final Set<String> BLOCKED =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws MissingFieldsException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED.contains(domain)) {
            throw new MissingFieldsException(List.of("email"));
        }
    }
}
