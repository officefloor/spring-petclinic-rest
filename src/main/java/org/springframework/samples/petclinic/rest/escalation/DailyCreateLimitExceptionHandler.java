package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DailyCreateLimitException;

public class DailyCreateLimitExceptionHandler {

    public void handle(@Parameter DailyCreateLimitException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.TOO_MANY_REQUESTS,
                "The daily owner creation limit has been reached");
        detail.setProperty("errors", List.of("registrationDate"));
        response.send(ProblemDetails.asResponse(detail));
    }
}
