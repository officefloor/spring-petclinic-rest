package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DuplicateOwnerIdentityException;

public class DuplicateOwnerIdentityExceptionHandler {

    public void handle(@Parameter DuplicateOwnerIdentityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with the same identity already exists");
        detail.setProperty("errors", List.of("identityKey"));
        response.send(ProblemDetails.asResponse(detail));
    }
}
