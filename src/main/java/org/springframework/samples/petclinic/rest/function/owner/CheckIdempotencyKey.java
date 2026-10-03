package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * Entry step for {@code POST /api/owners}. Decides the branch based on the request's
 * {@code Idempotency-Key}: a key already mapped to an owner replays that owner; otherwise
 * the create proceeds. This runs first so a replay short-circuits before the duplicate
 * check. Loading and rendering the replayed owner are the replay branch's own steps.
 */
public class CheckIdempotencyKey {

	/** Branch taken when the create should proceed normally. */
	@FunctionalInterface
	public interface ProceedFlow {
		void proceed();
	}

	/** Branch taken to replay the owner already created for this key. */
	@FunctionalInterface
	public interface ReplayFlow {
		void replay(Integer ownerId);
	}

	public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
			IdempotencyStore store, Out<String> idempotencyKeyOut,
			@Flow("proceed") ProceedFlow proceed, @Flow("replay") ReplayFlow replay) {

		// No key (or blank) => never idempotent; just create.
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			proceed.proceed();
			return;
		}

		// Already seen => replay the originally created owner.
		Integer existingId = store.find(idempotencyKey);
		if (existingId != null) {
			replay.replay(existingId);
			return;
		}

		// First time this key is seen: remember it for RecordIdempotencyKey, then create.
		idempotencyKeyOut.set(idempotencyKey);
		proceed.proceed();
	}
}
