package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class GeneralExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GeneralExceptionHandler.class);

    public void handle(@Parameter Exception ex, ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        logger.error("Unexpected error", ex);
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred while processing your request");
        response.send(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detail));
    }
}
