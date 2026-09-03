package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Reads the optional {@code Idempotency-Key} request header shared by the create-owner
 * pipeline's idempotency steps.
 */
public final class IdempotencyKey {

    static final String HEADER = "Idempotency-Key";

    private IdempotencyKey() {
    }

    /** The trimmed header value, or {@code null} when the header is absent or blank. */
    public static String read(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
