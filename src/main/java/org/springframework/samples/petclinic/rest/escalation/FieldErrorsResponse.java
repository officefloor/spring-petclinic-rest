package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Response body for a failed required-field validation: {@code {"errors": ["city", ...]}}.
 */
public record FieldErrorsResponse(List<String> errors) {
}
