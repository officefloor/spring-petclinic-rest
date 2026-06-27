package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class MethodArgumentNotValidExceptionHandler {

    public void handle(@Parameter MethodArgumentNotValidException e, ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setType(URI.create("about:blank"));
        detail.setTitle(e.getClass().getSimpleName());
        detail.setDetail("The request contains invalid or missing parameters");
        detail.setProperty("timestamp", Instant.now());

        BindingResult bindingResult = e.getBindingResult();
        List<ValidationMessageDto> schemaValidationErrors = bindingResult.getFieldErrors().stream()
            .map(fieldError -> {
                String rejectedValue = Objects.toString(fieldError.getRejectedValue(), "null");
                String defaultMessage = Objects.toString(fieldError.getDefaultMessage(), "Validation failed");
                String message = "Field '%s' %s (rejected value: %s)".formatted(
                    fieldError.getField(), defaultMessage, rejectedValue);
                return new ValidationMessageDto(message)
                    .putAdditionalProperty("field", fieldError.getField())
                    .putAdditionalProperty("rejectedValue", rejectedValue)
                    .putAdditionalProperty("defaultMessage", defaultMessage);
            })
            .toList();
        detail.setProperty("schemaValidationErrors", schemaValidationErrors);
        response.send(ResponseEntity.status(400).body(detail));
    }
}
