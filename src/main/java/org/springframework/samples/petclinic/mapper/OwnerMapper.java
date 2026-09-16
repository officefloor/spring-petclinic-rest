package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.AgeBand;
import org.springframework.samples.petclinic.model.CheckDigit;
import org.springframework.samples.petclinic.model.CustomerCode;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.model.Tenure;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.function.owner.OwnerTelephones;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(possibleDuplicate(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    /** Format an owner's name as 'LastName, FirstName' for display. */
    default String displayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /** The upper-cased first letters of firstName and lastName, dot-separated with a trailing dot. */
    default String initials(Owner owner) {
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    /** The stored E.164 telephone formatted for humans (see {@link OwnerTelephones#toDisplay}). */
    default String telephoneDisplay(Owner owner) {
        return OwnerTelephones.toDisplay(owner.getTelephone());
    }

    private static String initial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    /** The owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is the
     * last two digits of the registrationDate year. */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /** The owner's membership level from 1 to 4: starts at 1, plus 1 when an email is present,
     * plus 1 when namesakeCount is 0, plus 1 when tenure exceeds 365 days. The top level is thus
     * tenure-gated: a newly created owner has zero tenure, so it never exceeds level 3. */
    default int membershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        if (Tenure.days(owner.getRegistrationDate()) > 365) {
            level++;
        }
        return Math.min(level, 4);
    }

    /** The owner's canonical region: the REGION segment of the customer code (see
     * {@link CustomerCode}), or null when the customer code is unset. */
    default String locality(Owner owner) {
        return CustomerCode.region(owner.getCustomerCode());
    }

    /** The owner's preferred contact channel: 'EMAIL' when an email is present, otherwise 'PHONE'. */
    default String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /** The owner's derived identity key (see {@link OwnerIdentity}). */
    default String identityKey(Owner owner) {
        return OwnerIdentity.of(owner);
    }

    /** The Luhn check digit over the digits of the owner's customer code, or null when unset. */
    default Integer checkDigit(Owner owner) {
        return owner.getCustomerCode() == null ? null : CheckDigit.luhn(owner.getCustomerCode());
    }

    /** The owner's age band on their registration date, derived from their birth date (see
     * {@link AgeBand}), or null when either date is unset. */
    default String ageBand(Owner owner) {
        return AgeBand.on(owner.getBirthDate(), owner.getRegistrationDate());
    }

    /** Whether the owner soft-matched an existing owner at creation, i.e. it has a
     * {@code possibleDuplicateOf} id. */
    default boolean possibleDuplicate(Owner owner) {
        return owner.getPossibleDuplicateOf() != null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
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
