package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
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
    @Mapping(target = "membershipNumber", expression = "java(org.springframework.samples.petclinic.service.OwnerMembershipPolicy.membershipNumber(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "address", source = "address", qualifiedByName = "normalizeAddress")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Canonicalise the address on create: trim, collapse whitespace runs, upper-case, and expand
     * the common street-type abbreviations. The normalized value is what gets stored, returned, and
     * compared by the household rules.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        return address.trim().replaceAll("\\s+", " ").toUpperCase()
            .replaceAll("\\bST\\b", "STREET")
            .replaceAll("\\bRD\\b", "ROAD")
            .replaceAll("\\bAVE\\b", "AVENUE");
    }

    /** Store the telephone in E.164 form (see {@link E164Telephone#toE164}). */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return E164Telephone.toE164(telephone);
    }

    /** Lower-case the email so it is stored and returned in canonical form. */
    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase();
    }

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
