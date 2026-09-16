package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Reads the optional {@code Idempotency-Key} request header. Both the check that replays a
 * seen key ({@link CheckIdempotencyKey}) and the step that records a new one
 * ({@link RecordIdempotencyKey}) resolve the key the same way, so the header name and its
 * "absent or blank means none" parsing live here once.
 */
final class IdempotencyKeyHeader {

    static final String NAME = "Idempotency-Key";

    private IdempotencyKeyHeader() {
    }

    /** The trimmed key on the request, or {@code null} when the header is absent or blank. */
    static String read(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(NAME);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
