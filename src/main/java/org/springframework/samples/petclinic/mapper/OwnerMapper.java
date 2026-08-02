package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(OwnerMapper.initials(owner))")
    @Mapping(target = "membershipNumber", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "membershipTier", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /** First letters of first and last name, upper-cased and dot-separated with a trailing dot, e.g. "J.S.". */
    static String initials(Owner owner) {
        return (owner.getFirstName().substring(0, 1) + "." + owner.getLastName().substring(0, 1) + ".")
                .toUpperCase();
    }

    /**
     * The sequential membership number for the owner: one more than the number of owners that existed
     * before this one. Computed as the count of owners whose id is not greater than this owner's id.
     * Because a newly created owner always has the largest id, this equals the total owner count at
     * registration and stays stable when read back later.
     */
    static Integer membershipNumber(Owner owner, Collection<Owner> allOwners) {
        Integer id = owner.getId();
        if (id == null) {
            return null;
        }
        return (int) allOwners.stream()
                .filter(o -> o.getId() != null && o.getId() <= id)
                .count();
    }

    /**
     * The per-city customer code for the owner, formatted {@code "<UPPERCASE_CITY>-<NNNN>"} where NNNN
     * is one more than the number of owners already in that city, zero-padded to four digits (e.g.
     * "LONDON-0007"). Computed as the count of owners in the same city (case-insensitive) whose id is
     * not greater than this owner's id. Because a newly created owner always has the largest id, this
     * equals the number already in that city plus one at registration and stays stable when read back
     * later.
     */
    static String customerCode(Owner owner, Collection<Owner> allOwners) {
        Integer id = owner.getId();
        String city = owner.getCity();
        if (id == null || city == null) {
            return null;
        }
        long sequence = allOwners.stream()
                .filter(o -> o.getId() != null && o.getId() <= id && city.equalsIgnoreCase(o.getCity()))
                .count();
        return String.format("%s-%04d", city.toUpperCase(), sequence);
    }

    /**
     * The membership tier assigned to the owner at registration: {@code "FOUNDING"} for the first 100
     * owners ever created and {@code "STANDARD"} for all later owners. Derived from the sequential
     * {@link #membershipNumber(Owner, Collection) membership number}, so it is stable when read back later.
     */
    static String membershipTier(Owner owner, Collection<Owner> allOwners) {
        Integer number = membershipNumber(owner, allOwners);
        if (number == null) {
            return null;
        }
        return number <= 100 ? "FOUNDING" : "STANDARD";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    default OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
