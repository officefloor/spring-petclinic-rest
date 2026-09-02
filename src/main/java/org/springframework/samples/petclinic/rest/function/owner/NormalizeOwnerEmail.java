package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Optional email: when absent the request is untouched; when present it must be a syntactically
 * valid address, stored lower-cased in place so the entity and the response both carry it. An
 * invalid address rejects with 400 via {@link MissingOwnerFieldsException}.
 */
public class NormalizeOwnerEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new MissingOwnerFieldsException(List.of("email"));
        }
        request.setEmail(email.toLowerCase());
    }
}
