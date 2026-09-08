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

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's preferred contact channel: {@code 'EMAIL'} when an email address
     * is present (non-null and non-blank), otherwise {@code 'PHONE'}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's locality from the region its identity carries (see
     * {@link #localityRegion(Owner)}). Returns that region string, or {@code 'UNKNOWN'} when the
     * owner has no known region.
     */
    default String locality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = localityRegion(owner);
        return region != null ? region : "UNKNOWN";
    }

    /**
     * Resolves the region an owner's {@link #locality(Owner)} is derived from. This is the
     * {@code REGION} segment carried by the owner's {@code customerCode}, whose new
     * {@code '<REGION>-<HASH8>'} identity encodes the region ahead of the first {@code '-'}. The
     * sentinel {@code 'UNKNOWN'} region (used when the owner has no known region) and a missing
     * customer code both resolve to {@code null}. Keeping the region source in its own method leaves
     * {@link #locality(Owner)} owning only the {@code null -> 'UNKNOWN'} rendering, so where the
     * region itself comes from can change without disturbing that rendering.
     *
     * @param owner the owner whose locality region is resolved, never {@code null}
     * @return the region string, or {@code null} when the owner has no known region
     */
    default String localityRegion(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return null;
        }
        int dash = code.indexOf('-');
        String region = dash >= 0 ? code.substring(0, dash) : code;
        return "UNKNOWN".equals(region) ? null : region;
    }

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'}.
     */
    default String displayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Returns the owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    default String initials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
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
