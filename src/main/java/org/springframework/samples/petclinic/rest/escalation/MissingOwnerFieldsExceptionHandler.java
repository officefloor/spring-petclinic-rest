package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.MissingOwnerFieldsException;

/**
 * Responds 400 with a JSON body whose {@code errors} array lists each missing or blank
 * required owner field.
 */
public class MissingOwnerFieldsExceptionHandler {

    public void handle(@Parameter MissingOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<OwnerFieldErrors>> response) {
        response.send(ResponseEntity.badRequest().body(new OwnerFieldErrors(ex.getFields())));
    }
}
