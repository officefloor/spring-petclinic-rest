package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.InvalidEmailException;

/**
 * Responds 400 when an owner create request supplies an {@code email} that is not a
 * syntactically valid address. Mirrors the create endpoint's {@code errors} array contract
 * ({@link OwnerFieldErrors}) used for missing/blank fields and an invalid telephone, naming
 * {@code email}.
 */
public class InvalidEmailExceptionHandler {

    public void handle(@Parameter InvalidEmailException ex,
            ObjectResponse<ResponseEntity<OwnerFieldErrors>> response) {
        response.send(ResponseEntity.badRequest().body(new OwnerFieldErrors(List.of("email"))));
    }
}
