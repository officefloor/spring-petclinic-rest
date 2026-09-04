package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * When the create body carries an email, requires it to be a syntactically valid
 * address and stores it lower-cased. An absent or blank email is left untouched; an
 * invalid one is a 400 (reusing the missing-fields escalation). Runs before
 * {@link BuildOwner}, so the persisted value is already lower-cased.
 */
public class NormalizeOwnerEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new MissingOwnerFieldsException(List.of("email"));
        }
        request.setEmail(email.toLowerCase());
    }
}
