package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Single source of truth for the optional {@code Idempotency-Key} request header that drives the
 * idempotent {@code POST /api/owners} create: both the {@link CheckOwnerIdempotency} entry step and
 * the {@link RecordOwnerIdempotency} step read and normalize the header through here, so the header
 * name and the blank-handling rule cannot drift apart.
 *
 * <p>The starter does not bridge {@code @RequestHeader} into the Spring argument resolution for these
 * functions, so the header is read from the {@link HttpServletRequest} the starter does expose.
 */
public final class IdempotencyKey {

    private static final String HEADER = "Idempotency-Key";

    private IdempotencyKey() {
    }

    /** The {@code Idempotency-Key} header value, or {@code null} when it is absent or blank. */
    public static String fromRequest(HttpServletRequest request) {
        String key = request.getHeader(HEADER);
        return (key == null || key.isBlank()) ? null : key;
    }
}
