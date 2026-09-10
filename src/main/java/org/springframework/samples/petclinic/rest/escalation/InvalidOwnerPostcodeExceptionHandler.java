package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.InvalidOwnerPostcodeException;

public class InvalidOwnerPostcodeExceptionHandler {

    public void handle(@Parameter InvalidOwnerPostcodeException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The owner's postcode is invalid");
        detail.setProperty("errors", List.of("postcode"));
        response.send(ProblemDetails.asResponse(detail));
    }
}
