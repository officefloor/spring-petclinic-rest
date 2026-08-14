package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Runs in the create-owner pipeline after the required fields are validated. Email is optional:
 * a request that omits it (null or blank) passes through untouched. When present, the value must
 * be a syntactically valid address; it is then lower-cased and mutated into the validated body in
 * place so later steps build and store the normalized value. Rejects a present-but-invalid email
 * with a 400.
 */
public class NormalizeOwnerEmail {

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        request.setEmail(OwnerEmails.normalize(request.getEmail()));
    }
}
