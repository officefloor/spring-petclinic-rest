package org.springframework.samples.petclinic.rest.function.root;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RedirectToSwaggerTest {

    @Test
    void respondsFoundWithRelativeLocation() {
        MockObjectResponse<ResponseEntity<Void>> response = new MockObjectResponse<>();
        new RedirectToSwagger().service(response);

        ResponseEntity<Void> result = response.getObject();
        assertThat(result.getStatusCode().value()).isEqualTo(302);
        assertThat(result.getHeaders().getLocation().toString()).isEqualTo("swagger-ui/index.html");
    }
}
