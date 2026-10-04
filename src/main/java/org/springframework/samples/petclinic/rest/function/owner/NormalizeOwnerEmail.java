package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Email is optional on an owner. When absent (null or blank) this step does nothing.
 * When present it must be a syntactically valid address, otherwise it escalates as a
 * 400 via {@link InvalidEmailException}. A valid address is stored back on the owner
 * lower-cased (so it is persisted and returned lower-cased).
 */
public class NormalizeOwnerEmail {

    /**
     * Pragmatic syntactic check: a non-empty local part, a single '@', then a domain
     * with at least one dot and no whitespace.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val Owner owner) throws InvalidEmailException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        String lowered = trimmed.toLowerCase();
        String domain = OwnerEmails.domainOf(lowered);
        if (OwnerEmails.isDisposable(domain)) {
            throw new InvalidEmailException("Email domain is not allowed: " + domain);
        }
        owner.setEmail(lowered);
    }
}
