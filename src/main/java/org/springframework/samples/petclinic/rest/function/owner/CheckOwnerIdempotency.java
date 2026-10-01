package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.servlet.http.HttpServletRequest;
import net.officefloor.plugin.section.clazz.Flow;

/**
 * Entry point of the {@code POST /api/owners} pipeline that makes a create idempotent on the optional
 * {@code Idempotency-Key} request header.
 *
 * <p>When the request carries a key that has already created an owner (see {@link
 * OwnerIdempotencyStore}), the {@code replay} branch is taken, carrying the originally created owner's
 * id to {@link RespondWithOwnerReplay}, which returns that owner with 200 — no duplicate is created.
 * Otherwise (no key, or a key not seen before) the {@code create} branch runs the normal create
 * pipeline, which later records the key via {@link RecordOwnerIdempotency}.
 */
public class CheckOwnerIdempotency {

    @FunctionalInterface
    public interface CreateFlow {
        void flow();
    }

    @FunctionalInterface
    public interface ReplayFlow {
        void flow(Integer ownerId);
    }

    public void service(HttpServletRequest request, OwnerIdempotencyStore store,
            @Flow("create") CreateFlow createFlow, @Flow("replay") ReplayFlow replayFlow) {
        String idempotencyKey = IdempotencyKey.fromRequest(request);
        if (idempotencyKey != null) {
            Integer existingOwnerId = store.find(idempotencyKey);
            if (existingOwnerId != null) {
                replayFlow.flow(existingOwnerId); // key already seen: replay the original owner (200)
                return;
            }
        }
        createFlow.flow(); // new key (or none): run the normal create pipeline
    }
}
