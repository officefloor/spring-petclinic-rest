package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;
import org.springframework.util.StringUtils;

/**
 * Rejects an owner request whose email domain is on the disposable-domain blocklist with
 * 400. Runs after {@link NormalizeOwnerEmail} has validated the syntax and lower-cased the
 * address, so the domain is simply the text after the '@'. An absent email is allowed.
 */
public class RejectDisposableEmail {

    /** Domains that only ever host throwaway mailboxes. */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailException {
        String email = request.getEmail();
        if (!StringUtils.hasText(email)) {
            return;
        }
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new DisposableEmailException(domain);
        }
    }
}
