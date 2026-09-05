package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Adds idempotent-create semantics to {@code POST /api/owners}. This mapping only
 * matches when an {@code Idempotency-Key} header is present, so it is more specific
 * than {@link OwnerRestControllerV1#addOwner} (which handles the header-less case).
 *
 * <p>A first request with a given key is delegated to the normal create; the
 * resulting owner is remembered under that key. A repeat with an already-seen key
 * replays the originally created owner with {@code 200 OK} instead of creating a
 * duplicate.
 */
@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class IdempotentOwnerController {

    private final OwnerRestControllerV1 delegate;

    private final Map<String, OwnerDto> created = new ConcurrentHashMap<>();

    public IdempotentOwnerController(OwnerRestControllerV1 delegate) {
        this.delegate = delegate;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @PostMapping(value = "/owners", headers = "Idempotency-Key")
    public ResponseEntity<OwnerDto> addOwner(@RequestHeader("Idempotency-Key") String key,
                                             @Valid @RequestBody OwnerFieldsDto ownerFieldsDto) {
        OwnerDto existing = created.get(key);
        if (existing != null) {
            return new ResponseEntity<>(existing, HttpStatus.OK);
        }
        ResponseEntity<OwnerDto> response = delegate.addOwner(ownerFieldsDto);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            created.put(key, response.getBody());
        }
        return response;
    }
}
