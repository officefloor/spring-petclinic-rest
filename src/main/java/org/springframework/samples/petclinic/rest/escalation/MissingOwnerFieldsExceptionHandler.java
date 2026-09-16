package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

public class MissingOwnerFieldsExceptionHandler {

    public void handle(@Parameter MissingOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<MissingFieldsError>> response) {
        response.send(ResponseEntity.badRequest().body(new MissingFieldsError(ex.getFields())));
    }

    /** Response body listing, in {@code errors}, the name of each missing or blank field. */
    public record MissingFieldsError(List<String> errors) {
    }
}
