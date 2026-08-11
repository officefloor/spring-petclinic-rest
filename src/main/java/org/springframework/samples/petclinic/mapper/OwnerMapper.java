package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    @Autowired
    private OwnerRepository ownerRepository;

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipTier",
        expression = "java(membershipTier(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.mapper.Locality.of(owner.getCity()))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * Membership tier for an owner. {@code GOLD} takes precedence when the owner's household
     * (all owners sharing the same non-null {@code householdId}) has three or more members;
     * otherwise the {@code SILVER}/{@code BRONZE} rules apply: {@code SILVER} when the owner has
     * no namesakes and a non-blank email, {@code BRONZE} otherwise.
     */
    protected String membershipTier(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId != null && !householdId.isBlank()) {
            long members = ownerRepository.findAll().stream()
                .filter(other -> householdId.equals(other.getHouseholdId()))
                .count();
            if (members >= 3) {
                return "GOLD";
            }
        }
        return owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0
            && owner.getEmail() != null && !owner.getEmail().isBlank() ? "SILVER" : "BRONZE";
    }

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    public OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
