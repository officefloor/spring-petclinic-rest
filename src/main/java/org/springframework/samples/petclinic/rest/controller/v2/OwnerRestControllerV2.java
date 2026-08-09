package org.springframework.samples.petclinic.rest.controller.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.MembershipPoints;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.api.OwnerV2Api;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
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
        OwnerPageDto pageDto = ownerMapper.toOwnerPageDto(owners);
        applyMembership(pageDto.getContent(), owners.getContent());
        return new ResponseEntity<>(pageDto, HttpStatus.OK);
    }

    /**
     * Populates {@code membershipPoints} and {@code membershipLevel} on each dto in the page,
     * sizing every household from a single scan of all owners so the household-of-three-or-more
     * factor is scored correctly. The dtos and owners are in the same order.
     *
     * @param ownerDtos the page content to populate
     * @param owners    the owners the dtos were mapped from, in matching order
     */
    private void applyMembership(List<OwnerDto> ownerDtos, List<Owner> owners) {
        Map<String, Integer> householdSizes = new HashMap<>();
        for (Owner owner : this.clinicService.findAllOwners()) {
            if (owner.getHouseholdId() != null) {
                householdSizes.merge(owner.getHouseholdId(), 1, Integer::sum);
            }
        }
        for (int i = 0; i < ownerDtos.size() && i < owners.size(); i++) {
            Owner owner = owners.get(i);
            int householdSize = owner.getHouseholdId() == null ? 1
                : householdSizes.getOrDefault(owner.getHouseholdId(), 1);
            int points = MembershipPoints.points(owner, householdSize);
            int level = MembershipPoints.level(points);
            ownerDtos.get(i).setMembershipPoints(points);
            ownerDtos.get(i).setMembershipLevel(level);
            ownerDtos.get(i).setOwnerSegment(org.springframework.samples.petclinic.mapper.OwnerSegment
                .segment(level, ownerDtos.get(i).getLocality()));
        }
    }
}
