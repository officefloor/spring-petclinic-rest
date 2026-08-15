package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MissingOwnerFieldsExceptionHandler {

    /** Body shape: {@code {"errors": ["city", ...]}}. */
    public record MissingFields(List<String> errors) {
    }

    public void handle(@Parameter MissingOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<MissingFields>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MissingFields(ex.getFields())));
    }
}
