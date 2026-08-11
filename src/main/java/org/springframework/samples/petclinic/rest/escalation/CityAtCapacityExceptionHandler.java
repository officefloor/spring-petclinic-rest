package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CityAtCapacityExceptionHandler {

    public void handle(@Parameter CityAtCapacityException ex,
            ObjectResponse<ResponseEntity<Errors>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Errors(List.of("city"))));
    }

    /** Response body: {@code {"errors": ["city"]}}. */
    public record Errors(List<String> errors) {
    }
}
