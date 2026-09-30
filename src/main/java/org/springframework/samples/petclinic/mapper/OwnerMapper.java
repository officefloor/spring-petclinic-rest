package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.AgeBand;
import org.springframework.samples.petclinic.model.CityRegions;
import org.springframework.samples.petclinic.model.LuhnCheckDigit;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.model.OwnerTelephones;
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

    @Mapping(target = "telephoneDisplay", expression = "java(toTelephoneDisplay(owner))")
    @Mapping(target = "displayName", expression = "java(toDisplayName(owner))")
    @Mapping(target = "salutation", expression = "java(toSalutation(owner))")
    @Mapping(target = "initials", expression = "java(toInitials(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    @Mapping(target = "timezone", expression = "java(toTimezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(toContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(toIdentityKey(owner))")
    @Mapping(target = "checkDigit", expression = "java(toCheckDigit(owner))")
    @Mapping(target = "ageBand", expression = "java(toAgeBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's age band, derived by {@link AgeBand} from the owner's {@code birthDate}
     * against the effective {@code registrationDate}. {@code null} when the owner has no
     * birthDate (or no registrationDate to compute against).
     */
    default OwnerDto.AgeBandEnum toAgeBand(Owner owner) {
        AgeBand band = AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate());
        return (band == null) ? null : OwnerDto.AgeBandEnum.valueOf(band.name());
    }

    /**
     * A single Luhn check digit (0-9) computed over the digits of the owner's
     * {@code customerCode}. {@code null} when the owner has no customer code.
     */
    default Integer toCheckDigit(Owner owner) {
        return LuhnCheckDigit.of(owner.getCustomerCode());
    }

    /**
     * The owner's derived identityKey — the single value all duplicate detection is
     * expressed through. See {@link OwnerIdentity}.
     */
    default String toIdentityKey(Owner owner) {
        return OwnerIdentity.of(owner);
    }

    /**
     * The owner's locality: the region derived from the postcode by the shared
     * {@link CityRegions} table (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), or 'UNKNOWN' when
     * the postcode is absent or in no known range. This is the same value as the {@code <REGION>}
     * prefix of the owner's {@code customerCode}, so the locality follows the region-and-hash
     * identity rather than the city.
     */
    default String toLocality(Owner owner) {
        return CityRegions.identityRegion(owner.getPostcode());
    }

    /**
     * The owner's IANA timezone, derived from the same region as the {@link #toLocality(Owner)
     * locality} via the shared {@link CityRegions} region-to-timezone table
     * (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane).
     * {@code null} when the region is 'UNKNOWN' (postcode absent or in no known range).
     */
    default String toTimezone(Owner owner) {
        return CityRegions.timezoneOf(CityRegions.identityRegion(owner.getPostcode()));
    }

    /**
     * The owner's preferred contact channel: 'EMAIL' when an email is present,
     * otherwise 'PHONE'.
     */
    default OwnerDto.ContactPreferenceEnum toContactPreference(Owner owner) {
        String email = owner.getEmail();
        return (email != null && !email.isBlank())
                ? OwnerDto.ContactPreferenceEnum.EMAIL
                : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * The stored E.164 telephone formatted for humans by the shared {@link OwnerTelephones}
     * rule: country code, a space, then the national digits grouped in threes.
     */
    default String toTelephoneDisplay(Owner owner) {
        return OwnerTelephones.toDisplay(owner.getTelephone());
    }

    /** The owner's name formatted as 'LastName, FirstName'. */
    default String toDisplayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * The owner's salutation: the title followed by a single space and the lastName when a
     * title is supplied, otherwise just the lastName.
     */
    default String toSalutation(Owner owner) {
        String title = owner.getTitle();
        return (title != null && !title.isBlank())
                ? title + " " + owner.getLastName()
                : owner.getLastName();
    }

    /** The upper-cased first letters of firstName and lastName, dot-separated with a trailing dot. */
    default String toInitials(Owner owner) {
        return owner.getFirstName().substring(0, 1).toUpperCase()
                + "." + owner.getLastName().substring(0, 1).toUpperCase() + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "membershipNumber", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
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
