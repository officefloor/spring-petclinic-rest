package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.function.owner.AgeBand;
import org.springframework.samples.petclinic.rest.function.owner.CustomerCode;
import org.springframework.samples.petclinic.rest.function.owner.IdentityKey;
import org.springframework.samples.petclinic.rest.function.owner.Locality;
import org.springframework.samples.petclinic.rest.function.owner.Luhn;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(possibleDuplicate(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The owner's derived identity key: normalized telephone, email and household id joined. */
    default String identityKey(Owner owner) {
        return IdentityKey.of(owner);
    }

    /** Whether the owner was flagged as a possible duplicate at registration, i.e. it carries
     *  the id of an existing owner it possibly duplicates. */
    default boolean possibleDuplicate(Owner owner) {
        return owner.getPossibleDuplicateOf() != null;
    }

    /** Format an owner's name for display as 'LastName, FirstName'. */
    default String displayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /** The upper-cased first letters of firstName and lastName, dot-separated with a trailing dot (e.g. 'J.S.'). */
    default String initials(Owner owner) {
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private String initial(String name) {
        return name == null || name.isEmpty() ? "" : Character.toUpperCase(name.charAt(0)) + ".";
    }

    /** The owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is the
     *  last two digits of the registration date's year (e.g. 'SPR-SMI-0007-M26'). Absent until both the
     *  customer code and registration date have been assigned. */
    default String membershipNumber(Owner owner) {
        String customerCode = owner.getCustomerCode();
        LocalDate registrationDate = owner.getRegistrationDate();
        if (customerCode == null || registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }

    /** The Luhn check digit (0-9) over the digits of the owner's customerCode; absent until the
     *  customer code has been assigned. */
    default Integer checkDigit(Owner owner) {
        String customerCode = owner.getCustomerCode();
        return customerCode == null ? null : Luhn.checkDigit(customerCode);
    }

    /** The owner's region, read back from the region segment of its customerCode (the identity's
     *  {@code <REGION>-<HASH8>} form). Until the customer code has been assigned it falls back to
     *  deriving the region from the postcode range first (NSW 2000-2099, VIC 3000-3099,
     *  QLD 4000-4099), then the city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC,
     *  Brisbane-&gt;QLD), or 'UNKNOWN' otherwise. */
    default String locality(Owner owner) {
        String region = CustomerCode.region(owner.getCustomerCode());
        return region != null ? region : Locality.region(owner.getCity(), owner.getPostcode());
    }

    /** The owner's preferred contact channel: 'EMAIL' when an email address is on file,
     *  otherwise 'PHONE'. */
    default String contactPreference(Owner owner) {
        String email = owner.getEmail();
        return email != null && !email.isBlank() ? "EMAIL" : "PHONE";
    }

    /** The owner's age band at its registration date, derived from its birth date: 'MINOR'
     *  (under 18), 'ADULT' (18-64) or 'SENIOR' (65+); absent until a birth date is on file. */
    default String ageBand(Owner owner) {
        return AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate());
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
