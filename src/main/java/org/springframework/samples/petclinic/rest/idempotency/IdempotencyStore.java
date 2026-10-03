package org.springframework.samples.petclinic.rest.idempotency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner was created for a given {@code Idempotency-Key}, so a repeat
 * create with an already-seen key can return the original owner instead of creating a
 * duplicate.
 *
 * <p>The mapping is key &rarr; created owner id. It is a process-wide, in-memory record:
 * the key is a client-supplied token, not persisted state, so it does not belong in the
 * domain model.
 */
@Component
public class IdempotencyStore {

	private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

	/**
	 * Returns the id of the owner previously created for the key, or {@code null} if the
	 * key has not been seen.
	 */
	public Integer find(String key) {
		return (key == null) ? null : this.ownerIdByKey.get(key);
	}

	/** Records that {@code ownerId} was created for {@code key} (first writer wins). */
	public void record(String key, Integer ownerId) {
		if (key != null && ownerId != null) {
			this.ownerIdByKey.putIfAbsent(key, ownerId);
		}
	}
}
