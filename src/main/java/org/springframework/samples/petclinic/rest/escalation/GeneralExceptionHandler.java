package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public class GeneralExceptionHandler {

    public void handle(@Parameter Exception e, ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        detail.setType(URI.create("about:blank"));
        detail.setTitle(e.getClass().getSimpleName());
        detail.setDetail("An unexpected error occurred while processing your request");
        detail.setProperty("timestamp", Instant.now());
        detail.setProperty("schemaValidationErrors", List.<ValidationMessageDto>of());
        response.send(ResponseEntity.status(500).body(detail));
    }
}
