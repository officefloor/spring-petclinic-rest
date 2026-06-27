package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public class DataIntegrityViolationExceptionHandler {

    public void handle(@Parameter DataIntegrityViolationException e, ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setType(URI.create("about:blank"));
        detail.setTitle(e.getClass().getSimpleName());
        detail.setDetail("The requested resource could not be processed due to a data constraint violation");
        detail.setProperty("timestamp", Instant.now());
        detail.setProperty("schemaValidationErrors", List.<ValidationMessageDto>of());
        response.send(ResponseEntity.status(404).body(detail));
    }
}
