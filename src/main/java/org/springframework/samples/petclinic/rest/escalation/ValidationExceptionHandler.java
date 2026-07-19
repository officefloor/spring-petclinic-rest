package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Objects;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class ValidationExceptionHandler {

    public void handle(@Parameter MethodArgumentNotValidException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The request contains invalid or missing parameters");
        BindingResult bindingResult = ex.getBindingResult();
        if (bindingResult.hasErrors()) {
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
        }
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
