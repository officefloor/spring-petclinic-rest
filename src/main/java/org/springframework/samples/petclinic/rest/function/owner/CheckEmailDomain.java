package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Rejects a create whose email domain is a known disposable-mail provider, before
 * {@link SaveOwner} runs. Handled with 400 by {@code MissingOwnerFieldsHandler}, the
 * same 400 path {@link BuildOwner} uses for a malformed email. An absent email is left
 * untouched.
 */
public class CheckEmailDomain {

    private static final Set<String> BLOCKED = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val Owner owner) throws MissingOwnerFieldsException {
        String email = owner.getEmail();
        if (email == null) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at >= 0 && BLOCKED.contains(email.substring(at + 1).toLowerCase())) {
            throw new MissingOwnerFieldsException(List.of("email"));
        }
    }
}
