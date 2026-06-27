package org.springframework.samples.petclinic.rest.endpoint.v2;

import net.officefloor.web.ObjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.web.bind.annotation.RequestParam;

public class OwnerEndpointV2 {

    public void listOwnersPage(
            @RequestParam(name = "lastName", required = false) String lastName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            OwnerRepository ownerRepository,
            OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerPageDto>> response) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id"));
        Page<Owner> owners;
        if (lastName != null) {
            owners = ownerRepository.findByLastName(lastName, pageRequest);
        } else {
            owners = ownerRepository.findAll(pageRequest);
        }
        response.send(ResponseEntity.ok(ownerMapper.toOwnerPageDto(owners)));
    }
}
