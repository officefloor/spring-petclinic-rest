package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * After a successful create, remember which owner the request's {@code Idempotency-Key}
 * produced, so a later repeat with the same key replays this owner instead of creating a
 * duplicate. Absent when the request carried no key.
 */
public class RecordIdempotencyKey {

	public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore store) {
		store.record(idempotencyKey, owner.getId());
	}
}
