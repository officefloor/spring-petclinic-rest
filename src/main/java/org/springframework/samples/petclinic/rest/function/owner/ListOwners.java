package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Collection;
import java.util.List;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.web.bind.annotation.RequestParam;

public class ListOwners {

    public void service(@RequestParam(name = "lastName", required = false) String lastName,
            OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<List<OwnerDto>>> response) {
        Collection<Owner> owners = lastName != null
                ? ownerRepository.findByLastName(lastName)
                : ownerRepository.findAll();
        if (owners.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(ownerMapper.toOwnerDtoCollection(owners)));
    }
}
