package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.web.HttpHeaderParameter;

/**
 * Entry step of {@code POST /api/owners}: enforces create idempotency on the optional
 * {@code Idempotency-Key} header before any validation or persistence runs.
 *
 * <p>The header is optional — most creates omit it, in which case {@code idempotencyKey} is {@code null}
 * and the {@code create} flow runs the normal pipeline. When a (non-blank) key is present:
 * <ul>
 * <li>if it has already created an owner, the {@code replay} flow is taken with that owner id, so the
 * original owner is returned with 200 and no duplicate is created;</li>
 * <li>otherwise the key is published for {@link RecordIdempotency} to associate with the new owner once
 * saved, and the {@code create} flow runs.</li>
 * </ul>
 *
 * <p>Exactly one flow is invoked, so the two branches are mutually exclusive.
 */
public class CheckIdempotency {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            IdempotencyStore store, Out<IdempotencyKey> keyOut,
            @Flow("create") CreateFlow create, @Flow("replay") ReplayFlow replay) {

        String key = (idempotencyKey == null || idempotencyKey.isBlank()) ? null : idempotencyKey;
        if (key != null) {
            Integer existingId = store.find(key);
            if (existingId != null) {
                replay.replay(existingId); // key already created an owner: return it, do not create again
                return;
            }
        }
        keyOut.set(new IdempotencyKey(key)); // null when absent/blank: nothing to record later
        create.create();
    }
}
