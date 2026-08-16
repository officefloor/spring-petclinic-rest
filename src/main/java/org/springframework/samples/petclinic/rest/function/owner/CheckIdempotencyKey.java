package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * First step of the create pipeline: honours the optional {@code Idempotency-Key} request header so a
 * repeated create returns the originally created owner (200) instead of storing a duplicate (which
 * the identity/household checks would otherwise reject with 409).
 *
 * <p>When the header is present and already maps to a live owner (see {@link IdempotencyStore}), the
 * stored owner is sent back with 200 and the {@code create} flow is not taken — short-circuiting the
 * rest of the pipeline. Otherwise (no header, or a first-seen key) the {@code create} flow runs the
 * normal pipeline; {@link RecordIdempotencyKey} then records the key against the new owner so a later
 * repeat resolves here. A key whose stored owner has since been soft-deleted is treated as unseen, so
 * the create proceeds afresh.
 */
public class CheckIdempotencyKey {

    /** Header carrying the client-chosen idempotency token; matched case-insensitively. */
    static final String HEADER = "Idempotency-Key";

    public void service(ServerHttpConnection connection, IdempotencyStore idempotencyStore,
            OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            @Flow("create") CreateFlow create, ObjectResponse<ResponseEntity<OwnerDto>> response) {
        String key = key(connection);
        if (key != null) {
            Integer ownerId = idempotencyStore.lookup(key);
            if (ownerId != null) {
                Owner owner = ownerRepository.findById(ownerId);
                if (owner != null && !owner.isDeleted()) {
                    // Idempotent repeat: return the originally created owner with 200 and stop here.
                    OwnerDto dto = ownerMapper.toOwnerDto(owner);
                    dto.setBulkSignupWarning(BulkSignup.warningFor(ownerRepository));
                    Integer possibleDuplicateOf = PossibleDuplicate.matchFor(owner, ownerRepository);
                    dto.setPossibleDuplicate(possibleDuplicateOf != null);
                    dto.setPossibleDuplicateOf(possibleDuplicateOf);
                    response.send(ResponseEntity.ok(dto));
                    return;
                }
            }
        }
        // Not a repeat: run the normal create pipeline.
        create.create();
    }

    /** The trimmed {@code Idempotency-Key} value, or {@code null} when absent or blank. */
    static String key(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
