package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Region;
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

    OwnerDto toOwnerDto(Owner owner);

    /**
     * Fills the presentation-only {@code displayName} as {@code "LastName, FirstName"}
     * from the stored names, once MapStruct has mapped the remaining fields.
     */
    @AfterMapping
    default void addDisplayName(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setDisplayName(owner.getLastName() + ", " + owner.getFirstName());
    }

    /**
     * Fills the presentation-only {@code initials} as the upper-cased first letters of
     * firstName and lastName, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    @AfterMapping
    default void addInitials(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setInitials(
            Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".");
    }

    /**
     * Fills the {@code checkDigit} from the owner's domain-computed
     * {@link Owner#getCheckDigit()} (a Luhn check digit over the customerCode's digits).
     */
    @AfterMapping
    default void addCheckDigit(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setCheckDigit(owner.getCheckDigit());
    }

    /**
     * Fills the presentation-only {@code membershipNumber} as {@code '<customerCode>-M<YY>'}
     * where YY is the last two digits of the registrationDate year. Left null when either
     * source field is missing.
     */
    @AfterMapping
    default void addMembershipNumber(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setMembershipNumber(owner.getMembershipNumber());
    }

    /**
     * Fills the {@code membershipPoints} and {@code membershipLevel} from the owner's
     * domain-computed {@link Owner#getMembershipPoints()} and {@link Owner#getMembershipLevel()}.
     */
    @AfterMapping
    default void addMembershipLevel(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setMembershipPoints(owner.getMembershipPoints());
        ownerDto.setMembershipLevel(owner.getMembershipLevel());
    }

    /**
     * Fills the {@code contactPreference} as {@code EMAIL} when the owner has an email
     * address, otherwise {@code PHONE}.
     */
    @AfterMapping
    default void addContactPreference(Owner owner, @MappingTarget OwnerDto ownerDto) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        ownerDto.setContactPreference(
            hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE);
    }

    /**
     * Fills the presentation-only {@code locality} as the {@code <REGION>} component of the owner's
     * {@code customerCode} (see {@link Region#regionOfCustomerCode(String)}) — the region-and-hash
     * identity is the single source of truth — or {@code 'UNKNOWN'} when no customerCode is assigned.
     */
    @AfterMapping
    default void addLocality(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setLocality(Region.regionOfCustomerCode(owner.getCustomerCode()));
    }

    /**
     * Fills the presentation-only {@code timezone} as the IANA timezone name derived from the
     * owner's locality/region (the {@code <REGION>} component of its {@code customerCode}) via the
     * fixed region-to-timezone table (see {@link Region#timezoneForRegion(String)}), or {@code null}
     * when the region has no mapped timezone.
     */
    @AfterMapping
    default void addTimezone(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setTimezone(Region.timezoneForRegion(Region.regionOfCustomerCode(owner.getCustomerCode())));
    }

    /**
     * Fills the {@code ageBand} from the owner's domain-computed {@link Owner#getAgeBand()}
     * (derived from birthDate measured against registrationDate), leaving it null when no
     * birth date is available.
     */
    @AfterMapping
    default void addAgeBand(Owner owner, @MappingTarget OwnerDto ownerDto) {
        String ageBand = owner.getAgeBand();
        if (ageBand != null) {
            ownerDto.setAgeBand(OwnerDto.AgeBandEnum.fromValue(ageBand));
        }
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
