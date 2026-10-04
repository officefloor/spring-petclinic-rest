package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.InvalidTelephoneException;

/**
 * Responds 400 when an owner create request's telephone is not exactly ten digits after
 * stripping non-digit characters. Mirrors the create endpoint's {@code errors} array contract
 * ({@link OwnerFieldErrors}) used for missing/blank fields, naming {@code telephone}.
 */
public class InvalidTelephoneExceptionHandler {

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<OwnerFieldErrors>> response) {
        response.send(ResponseEntity.badRequest().body(new OwnerFieldErrors(List.of("telephone"))));
    }
}
