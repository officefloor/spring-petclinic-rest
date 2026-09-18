package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.stereotype.Component;

/**
 * Remembers which owner was created for each seen {@code Idempotency-Key}, so a create that
 * repeats with an already-seen key can return the original owner instead of creating a duplicate.
 *
 * <p>Deliberately in-memory and non-transactional: the mapping must survive the request that first
 * stored it (and its transaction) for a later repeat to be recognised, so it is not rolled back with
 * the create.
 */
@Component
public class IdempotencyStore {

    /** Request header carrying the client-chosen idempotency key; absent means no idempotency. */
    public static final String HEADER = "Idempotency-Key";

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The idempotency key on the request, or empty when the header is absent or blank. */
    public static Optional<String> keyOf(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return Optional.empty();
        }
        String value = header.getValue();
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.trim());
    }

    /** The id of the owner previously created for {@code key}, or empty when the key is new. */
    public Optional<Integer> find(String key) {
        return Optional.ofNullable(ownerIdByKey.get(key));
    }

    /** Record that {@code key} first created the owner with {@code ownerId}. */
    public void record(String key, int ownerId) {
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
