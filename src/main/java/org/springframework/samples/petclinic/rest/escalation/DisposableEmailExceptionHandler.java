package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DisposableEmailException;

/**
 * Responds 400 when an owner create request supplies an {@code email} whose domain is on the
 * disposable-email blocklist. Mirrors the create endpoint's {@code errors} array contract
 * ({@link OwnerFieldErrors}) used for missing/blank fields and an invalid telephone, naming
 * {@code email}.
 */
public class DisposableEmailExceptionHandler {

    public void handle(@Parameter DisposableEmailException ex,
            ObjectResponse<ResponseEntity<OwnerFieldErrors>> response) {
        response.send(ResponseEntity.badRequest().body(new OwnerFieldErrors(List.of("email"))));
    }
}
