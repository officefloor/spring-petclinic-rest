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

    /**
     * The number of members a household must reach for its owners to be promoted to the {@code GOLD}
     * membership tier. A household is the set of owners sharing the same non-blank {@code householdId}.
     */
    private static final int GOLD_HOUSEHOLD_SIZE = 3;

    @Autowired
    protected OwnerRepository ownerRepository;

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "membershipTier", expression = "java(membershipTier(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.of(owner.getCity()))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Computes an owner's membership tier. An owner is {@code GOLD} when its household (the owners sharing
     * the same non-blank {@code householdId}) has {@value #GOLD_HOUSEHOLD_SIZE} or more members; otherwise
     * the owner is {@code SILVER} when its {@code namesakeCount} is 0 and an email is present, and
     * {@code BRONZE} in all remaining cases.
     *
     * @param owner the owner whose tier is being computed
     * @return the membership tier ({@code GOLD}, {@code SILVER} or {@code BRONZE})
     */
    protected String membershipTier(Owner owner) {
        if (isGoldHousehold(owner)) {
            return "GOLD";
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0
            && owner.getEmail() != null && !owner.getEmail().isBlank()) {
            return "SILVER";
        }
        return "BRONZE";
    }

    /**
     * Determines whether the owner's household has reached the {@code GOLD} threshold, i.e. at least
     * {@value #GOLD_HOUSEHOLD_SIZE} owners share the owner's non-blank {@code householdId}. An owner
     * without a household id is never gold.
     *
     * @param owner the owner being evaluated
     * @return {@code true} when the owner's household has {@value #GOLD_HOUSEHOLD_SIZE} or more members
     */
    private boolean isGoldHousehold(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return false;
        }
        long members = this.ownerRepository.findAll().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
        return members >= GOLD_HOUSEHOLD_SIZE;
    }

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
