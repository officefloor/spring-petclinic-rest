package org.springframework.samples.petclinic.rest.function.root;

import java.net.URI;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Redirects context-root requests to the Swagger UI. The Location is context-relative
 * ("swagger-ui/index.html", no leading slash) so it resolves correctly under the
 * server.servlet.context-path without needing to inject it explicitly.
 */
public class RedirectToSwagger {

    public void service(ObjectResponse<ResponseEntity<Void>> response) {
        response.send(ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("swagger-ui/index.html"))
                .build());
    }
}
