package org.springframework.samples.petclinic.rest.controller.v2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.api.OwnerV2Api;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestControllerV2 implements OwnerV2Api {

    private final ClinicService clinicService;
    private final OwnerMapper ownerMapper;

    public OwnerRestControllerV2(ClinicService clinicService, OwnerMapper ownerMapper) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
    }

    @Override
    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    public ResponseEntity<OwnerPageDto> listOwnersPage(String lastName, Integer page, Integer size) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 20 : size;
        Page<Owner> owners = this.clinicService.findOwners(
            lastName,
            PageRequest.of(pageNumber, pageSize, Sort.by("id")));
        owners.forEach(owner -> owner.setHouseholdMemberCount(countHouseholdMembers(owner)));
        return new ResponseEntity<>(ownerMapper.toOwnerPageDto(owners), HttpStatus.OK);
    }

    /**
     * Counts the members of the given owner's household - the owners that share this owner's
     * {@code householdId}, including the owner itself. An owner with no household is its own sole
     * member.
     *
     * @param owner the owner whose household is being sized
     * @return the number of owners sharing this owner's household (one when it has no household)
     */
    private int countHouseholdMembers(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 1;
        }
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }
}
