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
 * Maps Owner &amp; OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    @Autowired
    protected OwnerRepository ownerRepository;

    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipTier",
            expression = "java(membershipTier(owner))")
    @Mapping(target = "locality",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.Localities.of(owner.getCity()))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    public abstract OwnerDto toOwnerDto(Owner owner);

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

    /**
     * Derive the owner's membership tier. An owner whose household (owners sharing the same
     * {@code householdId}) has three or more members is {@code GOLD}; otherwise the existing
     * {@code SILVER} (no namesakes and an email on file) and {@code BRONZE} rules apply.
     */
    protected String membershipTier(Owner owner) {
        if (owner.getHouseholdId() != null && householdSize(owner.getHouseholdId()) >= 3) {
            return "GOLD";
        }
        boolean silver = owner.getNamesakeCount() != null && owner.getNamesakeCount().intValue() == 0
                && owner.getEmail() != null && !owner.getEmail().isEmpty();
        return silver ? "SILVER" : "BRONZE";
    }

    /** Count the owners currently stored that share the given household id. */
    private long householdSize(String householdId) {
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }
}
