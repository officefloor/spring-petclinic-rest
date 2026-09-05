package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Reads the optional {@code Idempotency-Key} request header. A plain utility (not an
 * OfficeFloor function), so both {@link CheckIdempotencyKey} and {@link RecordIdempotencyKey}
 * read the header the same way. The header is optional: a request without it (or with a blank
 * value) has no idempotency key and creates normally.
 */
public final class IdempotencyKeys {

    static final String HEADER = "Idempotency-Key";

    private IdempotencyKeys() {
    }

    /** The trimmed {@code Idempotency-Key} value, or {@code null} when absent or blank. */
    public static String from(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
