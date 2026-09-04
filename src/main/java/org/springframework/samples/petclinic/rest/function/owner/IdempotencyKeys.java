package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Reads the optional {@code Idempotency-Key} request header. The header drives the
 * idempotent-create rule (see {@link CheckIdempotencyKey} and {@link RecordIdempotencyKey}):
 * it is read directly off the connection so an absent header is simply {@code null} rather
 * than a binding failure.
 */
final class IdempotencyKeys {

    static final String HEADER = "Idempotency-Key";

    private IdempotencyKeys() {
    }

    /** The trimmed non-blank header value, or {@code null} when the header is absent or blank. */
    static String read(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
