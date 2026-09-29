package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 when a create/update-owner postcode is out of range for the owner's city region,
 * mirroring {@link InvalidTelephoneExceptionHandler}'s {@code errors} array shape.
 */
public class PostcodeOutOfRangeExceptionHandler {

    public void handle(@Parameter PostcodeOutOfRangeException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("errors", List.of("postcode"))));
    }
}
