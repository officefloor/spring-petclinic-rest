package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Remembers the owner id created for each seen {@code Idempotency-Key}, so a create that repeats
 * with an already-seen key returns the originally created owner instead of creating a duplicate.
 */
public final class IdempotencyStore {

    private static final String HEADER = "Idempotency-Key";

    private static final Map<String, Integer> BY_KEY = new ConcurrentHashMap<>();

    private IdempotencyStore() {
    }

    /** The request's {@code Idempotency-Key} header value, or {@code null} when absent. */
    static String keyOf(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        return header == null ? null : header.getValue();
    }

    /** The owner id previously created under {@code key}, or {@code null} if none (or key null). */
    static Integer find(String key) {
        return key == null ? null : BY_KEY.get(key);
    }

    /** Remember {@code ownerId} under {@code key} on first use; a no-op when {@code key} is null. */
    static void record(String key, int ownerId) {
        if (key != null) {
            BY_KEY.putIfAbsent(key, ownerId);
        }
    }
}
