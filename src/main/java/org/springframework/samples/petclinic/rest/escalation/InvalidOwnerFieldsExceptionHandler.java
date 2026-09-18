package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;

/**
 * Responds 400 Bad Request as an RFC7807 {@code application/problem+json} body whose
 * {@code schemaValidationErrors} array names each invalid or missing field, e.g. an entry
 * carrying {@code "field":"city"}.
 */
public class InvalidOwnerFieldsExceptionHandler {

    public void handle(@Parameter InvalidOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The request contains invalid or missing fields");
        List<ValidationMessageDto> schemaValidationErrors = ex.getFields().stream()
                .map(field -> new ValidationMessageDto("Field '%s' is invalid or missing".formatted(field))
                        .putAdditionalProperty("field", field))
                .toList();
        detail.setProperty("schemaValidationErrors", schemaValidationErrors);
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
