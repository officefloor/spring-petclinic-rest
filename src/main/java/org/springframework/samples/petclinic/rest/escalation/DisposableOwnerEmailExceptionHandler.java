package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DisposableOwnerEmailException;

public class DisposableOwnerEmailExceptionHandler {

    public void handle(@Parameter DisposableOwnerEmailException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The owner's email address is not from an acceptable domain");
        detail.setProperty("errors", List.of("email"));
        response.send(ProblemDetails.asResponse(detail));
    }
}
