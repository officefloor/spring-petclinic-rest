package org.springframework.samples.petclinic.rest.function.common;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

public class RespondWithNoContent {

    public void service(ObjectResponse<ResponseEntity<Void>> response) {
        response.send(ResponseEntity.noContent().build());
    }
}
