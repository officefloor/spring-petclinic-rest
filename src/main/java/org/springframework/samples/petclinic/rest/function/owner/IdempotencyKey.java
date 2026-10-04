package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * The {@code Idempotency-Key} request header for {@code POST /owners}: its name and the rules for
 * reading it. Kept separate from {@link IdempotencyStore} (the cache) so header parsing and result
 * caching stay distinct responsibilities.
 *
 * <p>The value is read from {@link ServerHttpConnection} rather than via Spring MVC's
 * {@code @RequestHeader}, because in this OfficeFloor runtime request headers are carried on the
 * connection and are not surfaced on the {@code HttpServletRequest} that Spring's argument
 * resolvers read.
 */
final class IdempotencyKey {

    private static final String HEADER = "Idempotency-Key";

    private IdempotencyKey() {
    }

    /**
     * Reads the idempotency key from the request.
     *
     * @param connection current {@link ServerHttpConnection}.
     * @return the trimmed key, or {@code null} when the header is absent or blank.
     */
    static String fromRequest(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
