package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomerCodeCollisionTests {

    private final OwnerRepository ownerRepository = mock(OwnerRepository.class);

    private final ClinicServiceImpl service = new ClinicServiceImpl(mock(PetRepository.class),
        mock(VetRepository.class), ownerRepository, mock(VisitRepository.class),
        mock(SpecialtyRepository.class), mock(PetTypeRepository.class));

    private Owner newOwner() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setCity("Sydney");
        owner.setPostcode("2000");
        owner.setTelephone("6085551023");
        return owner;
    }

    @Test
    void keepsComputedCodeWhenUnique() {
        Owner owner = newOwner();
        service.saveOwner(owner);
        assertTrue(owner.getCustomerCode().matches("NSW-[0-9A-F]{8}"), owner.getCustomerCode());
    }

    @Test
    void appendsTwoOnFirstCollision() {
        Owner probe = newOwner();
        service.saveOwner(probe);
        String base = probe.getCustomerCode();

        when(ownerRepository.countByCustomerCode(anyString())).thenReturn(0L);
        when(ownerRepository.countByCustomerCode(base)).thenReturn(1L);
        Owner owner = newOwner();
        service.saveOwner(owner);
        assertEquals(base + "-2", owner.getCustomerCode());
    }

    @Test
    void appendsSmallestFreeSuffix() {
        Owner probe = newOwner();
        service.saveOwner(probe);
        String base = probe.getCustomerCode();

        when(ownerRepository.countByCustomerCode(anyString())).thenReturn(0L);
        when(ownerRepository.countByCustomerCode(base)).thenReturn(1L);
        when(ownerRepository.countByCustomerCode(base + "-2")).thenReturn(1L);
        when(ownerRepository.countByCustomerCode(base + "-3")).thenReturn(1L);
        Owner owner = newOwner();
        service.saveOwner(owner);
        assertEquals(base + "-4", owner.getCustomerCode());
    }
}
