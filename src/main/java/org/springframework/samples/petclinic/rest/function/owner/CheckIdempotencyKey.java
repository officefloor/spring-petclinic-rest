package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * First step of create-owner: honours the optional {@code Idempotency-Key} header. When a key has
 * already been seen (see {@link OwnerIdempotency}), responds {@code 200} with the originally
 * created owner and short-circuits — no duplicate is created. Otherwise it publishes the key for
 * {@link AssignOwnerIdempotencyKey} and takes the {@code create} flow into the normal pipeline.
 *
 * <p>Runs before the body is bound, so it never consumes the request body; the {@code create}
 * branch leads to {@link NormalizeOwnerAddress}, which binds it once for the pipeline.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface CreateFlow {
        void create();
    }

    public void service(@HttpHeaderParameter("Idempotency-Key") String key,
            OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            Out<RequestIdempotencyKey> idempotencyKey, ObjectResponse<OwnerDto> response,
            @Flow("create") CreateFlow create) {
        Owner existing = OwnerIdempotency.findByKey(ownerRepository, key);
        if (existing != null) {
            response.send(ownerMapper.toOwnerDto(existing));
            return;
        }
        idempotencyKey.set(new RequestIdempotencyKey(key));
        create.create();
    }
}
