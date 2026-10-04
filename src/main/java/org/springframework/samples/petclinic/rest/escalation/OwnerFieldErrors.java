package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Error response body for a rejected owner create: {@code {"errors": ["city", ...]}}.
 *
 * <p>This is a deliberate, spec-mandated divergence from the project's usual
 * {@link org.springframework.http.ProblemDetail} error contract: the create endpoint's
 * specification requires a top-level {@code errors} array naming each missing or blank
 * required field, which {@code ProblemDetail} (and its {@code schemaValidationErrors})
 * does not express.
 */
public class OwnerFieldErrors {

    private final List<String> errors;

    public OwnerFieldErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return this.errors;
    }
}
