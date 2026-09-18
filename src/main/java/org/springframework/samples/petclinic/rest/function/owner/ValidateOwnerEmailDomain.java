package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerFieldsException;

/**
 * Runs after {@link NormalizeOwnerEmail} in the owner pipelines. When the body carries a
 * present email whose domain is on the disposable-domain blocklist it throws
 * {@link InvalidOwnerFieldsException} naming {@code email}, so the response is a 400. An
 * absent email is skipped; format validity was already enforced upstream. It reads the
 * already-normalized body as a variable rather than binding {@code @RequestBody}.
 */
public class ValidateOwnerEmailDomain {

    public void service(@Val OwnerFieldsDto request) throws InvalidOwnerFieldsException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        if (OwnerEmail.isDisposable(email)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
    }
}
