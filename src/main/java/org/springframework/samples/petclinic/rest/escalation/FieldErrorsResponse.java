package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Error body naming the request fields that failed validation, serialized as
 * {@code {"errors": ["city", ...]}}.
 */
public record FieldErrorsResponse(List<String> errors) {
}
