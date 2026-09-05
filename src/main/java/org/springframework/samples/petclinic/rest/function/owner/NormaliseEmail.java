package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Email is optional. When an owner request carries one it must be a syntactically valid
 * address; it is then stored lower-cased. An absent email is left untouched. Mutates the
 * published body in place, so the mapper and later steps see the normalised value.
 */
public class NormaliseEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new MissingOwnerFieldsException(List.of("email"));
        }
        request.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
