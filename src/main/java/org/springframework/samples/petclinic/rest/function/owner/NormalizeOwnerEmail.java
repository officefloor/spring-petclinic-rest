package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes an owner's optional email. When absent the owner is left untouched; when present the
 * value must be a syntactically valid address, otherwise a 400 is raised. The stored (and later
 * returned) value is lower-cased. Mutates the {@link Owner} in place so {@link SaveOwner} persists
 * the normalized value.
 */
public class NormalizeOwnerEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val Owner owner) throws InvalidEmailException {
        String email = owner.getEmail();
        if (email == null) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        owner.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
