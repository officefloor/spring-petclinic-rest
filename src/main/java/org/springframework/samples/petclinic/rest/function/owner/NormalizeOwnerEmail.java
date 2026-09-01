package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Email is optional: a missing (null/blank) value is left untouched. When present it must be a
 * syntactically valid address, otherwise the request is rejected 400 via
 * {@link MissingOwnerFieldsException}. A valid value is lower-cased in place, so later steps save
 * and return it as {@code email}.
 */
public class NormalizeOwnerEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val Owner owner) throws MissingOwnerFieldsException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new MissingOwnerFieldsException(List.of("email"));
        }
        owner.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
