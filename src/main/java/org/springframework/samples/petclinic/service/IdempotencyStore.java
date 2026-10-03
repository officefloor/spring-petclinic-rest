package org.springframework.samples.petclinic.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each seen {@code Idempotency-Key} so a repeated create
 * with the same key can replay the original owner instead of creating a duplicate.
 *
 * <p>A stateful Spring singleton collaborator (not an OfficeFloor pipeline step): keyed by
 * the raw header value, holding the created owner id, so the mapping outlives a single
 * request. {@link #record} keeps the first id seen for a key.
 */
@Component
public class IdempotencyStore {

    /** Name of the request header carrying the idempotency key. */
    public static final String HEADER = "Idempotency-Key";

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The {@code Idempotency-Key} header value on the request, or {@code null} if absent. */
    public static String keyFrom(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        return header == null ? null : header.getValue();
    }

    /** The owner id previously created for {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return key == null ? null : this.keyToOwnerId.get(key);
    }

    /** Remember the owner {@code id} created for {@code key} (first id wins). */
    public void record(String key, Integer id) {
        if (key != null && id != null) {
            this.keyToOwnerId.putIfAbsent(key, id);
        }
    }
}
