package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * First step of {@code POST /api/owners}: honours an optional {@code Idempotency-Key} header. When
 * the key has already been recorded for a completed create, this responds 200 with the originally
 * created owner and does NOT trigger the {@code proceed} branch, so the create pipeline is skipped
 * and no duplicate is made (nor any later duplicate/quota rule applied). Otherwise it triggers
 * {@code proceed} to run the normal create pipeline, after which {@link RegisterIdempotency} records
 * the key against the newly saved owner.
 */
public class CheckIdempotencyKey {

    /** Client-supplied header naming the idempotency key. */
    public static final String HEADER = "Idempotency-Key";

    /** The {@code proceed} branch: run the normal create pipeline (wired to {@code validate}). */
    @FunctionalInterface
    public interface ProceedFlow {
        void proceed();
    }

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            @Flow("proceed") ProceedFlow proceed, ObjectResponse<OwnerDto> response) {
        String key = header(connection);
        if (key != null) {
            Integer ownerId = store.find(key);
            if (ownerId != null) {
                Owner existing = findQuietly(ownerRepository, ownerId);
                if (existing != null) {
                    response.send(ownerMapper.toOwnerDto(existing)); // 200; no duplicate created
                    return;
                }
            }
        }
        proceed.proceed();
    }

    /** The trimmed {@code Idempotency-Key} value, or {@code null} when absent or blank. */
    static String header(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    private static Owner findQuietly(OwnerRepository ownerRepository, Integer ownerId) {
        try {
            return ownerRepository.findById(ownerId);
        }
        catch (DataAccessException ex) {
            return null; // recorded owner no longer present; fall through to a fresh create
        }
    }
}
