package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.web.ObjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.web.bind.annotation.RequestParam;

public class ListOwnersPage {

    public void service(
            @RequestParam(name = "lastName", required = false) String lastName,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            ObjectResponse<OwnerPageDto> response) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size, Sort.by("id"));
        Page<Owner> owners = lastName != null
                ? ownerRepository.findByLastName(lastName, pageable)
                : ownerRepository.findAll(pageable);
        HouseholdMembers.stampAll(owners.getContent(), ownerRepository);
        response.send(ownerMapper.toOwnerPageDto(owners));
    }
}
