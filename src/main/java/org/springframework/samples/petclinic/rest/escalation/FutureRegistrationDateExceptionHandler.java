package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.FutureRegistrationDateException;

/**
 * Responds 400 when an owner create request's supplied {@code registrationDate} is later than the
 * server date. Mirrors the create endpoint's {@code errors} array contract ({@link OwnerFieldErrors})
 * used for missing/blank and other invalid fields, naming {@code registrationDate}.
 */
public class FutureRegistrationDateExceptionHandler {

    public void handle(@Parameter FutureRegistrationDateException ex,
            ObjectResponse<ResponseEntity<OwnerFieldErrors>> response) {
        response.send(ResponseEntity.badRequest().body(new OwnerFieldErrors(List.of("registrationDate"))));
    }
}
