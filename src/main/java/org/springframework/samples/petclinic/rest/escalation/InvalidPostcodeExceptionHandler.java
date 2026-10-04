package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.InvalidPostcodeException;

/**
 * Responds 400 when an owner create request's supplied {@code postcode} is not four digits or is
 * out of range for the city's region. Mirrors the create endpoint's {@code errors} array contract
 * ({@link OwnerFieldErrors}) used for missing/blank and other invalid fields, naming
 * {@code postcode}.
 */
public class InvalidPostcodeExceptionHandler {

    public void handle(@Parameter InvalidPostcodeException ex,
            ObjectResponse<ResponseEntity<OwnerFieldErrors>> response) {
        response.send(ResponseEntity.badRequest().body(new OwnerFieldErrors(List.of("postcode"))));
    }
}
