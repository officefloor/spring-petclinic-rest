package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.OwnerMapperImpl;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BuildOwnerTest {

    @Test
    void mapsRequestToNewOwnerWithNoId() {
        OwnerFieldsDto request = new OwnerFieldsDto();
        request.setFirstName("George");
        request.setLastName("Franklin");
        request.setAddress("110 W. Liberty St.");
        request.setCity("Madison");
        request.setTelephone("6085551023");

        MockVar<Owner> built = new MockVar<>();
        new BuildOwner().service(request, new OwnerMapperImpl(), built);

        Owner owner = built.get();
        assertNull(owner.getId());
        assertEquals("George", owner.getFirstName());
        assertEquals("Franklin", owner.getLastName());
        assertEquals("110 W. Liberty St.", owner.getAddress());
        assertEquals("Madison", owner.getCity());
        assertEquals("6085551023", owner.getTelephone());
    }
}
