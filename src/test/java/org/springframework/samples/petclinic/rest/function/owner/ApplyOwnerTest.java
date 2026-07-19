package org.springframework.samples.petclinic.rest.function.owner;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

import static org.assertj.core.api.Assertions.assertThat;

class ApplyOwnerTest {

    @Test
    void copiesFieldsOntoTheEntity() {
        Owner owner = new Owner();
        owner.setFirstName("old");
        OwnerFieldsDto request = new OwnerFieldsDto()
            .firstName("new").lastName("Franklin").address("addr").city("city").telephone("1112223333");

        new ApplyOwner().service(owner, request);

        assertThat(owner.getFirstName()).isEqualTo("new");
        assertThat(owner.getLastName()).isEqualTo("Franklin");
        assertThat(owner.getAddress()).isEqualTo("addr");
        assertThat(owner.getCity()).isEqualTo("city");
        assertThat(owner.getTelephone()).isEqualTo("1112223333");
    }
}
