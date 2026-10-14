package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.validation.OwnerPostcode;
import org.springframework.samples.petclinic.rest.validation.OwnerTelephone;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    Map<String, String> CITY_REGIONS = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(toInitials(owner))")
    @Mapping(target = "checkDigit", expression = "java(toCheckDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(toMembershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(toMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    @Mapping(target = "contactPreference", expression = "java(toContactPreference(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(Boolean.TRUE.equals(owner.getBulkSignupWarning()))")
    @Mapping(target = "possibleDuplicate", expression = "java(owner.getPossibleDuplicateOf() != null)")
    @Mapping(target = "ageBand", expression = "java(toAgeBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(toTelephoneDisplay(owner))")
    @Mapping(target = "sharesHousehold", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The stored E.164 telephone formatted for humans, e.g. '+61 412 345 678'.
     */
    default String toTelephoneDisplay(Owner owner) {
        return OwnerTelephone.format(owner.getTelephone());
    }

    /**
     * The owner's age in completed years on the registration date: 'MINOR' under 18, 'ADULT' 18-64,
     * 'SENIOR' 65 or over. Absent when the owner has no birth date or registration date.
     */
    default String toAgeBand(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        long age = ChronoUnit.YEARS.between(owner.getBirthDate(), owner.getRegistrationDate());
        if (age < 18) {
            return "MINOR";
        }
        return age < 65 ? "ADULT" : "SENIOR";
    }

    /**
     * The REGION part of the owner's customer code ('&lt;REGION&gt;-&lt;HASH8&gt;'); for owners without a
     * customer code, the region derived by {@link OwnerPostcode#regionOf(String, String)}.
     */
    default String toLocality(Owner owner) {
        String code = owner.getCustomerCode();
        int dash = code == null ? -1 : code.lastIndexOf('-');
        if (dash > 0) {
            return code.substring(0, dash);
        }
        return OwnerPostcode.regionOf(owner.getPostcode(), owner.getCity());
    }

    /**
     * 'EMAIL' when the owner has an email address, otherwise 'PHONE'.
     */
    default String toContactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }

    /** The highest membership level assigned on create; level 4 is reserved for tenure. */
    int MAX_CREATE_MEMBERSHIP_LEVEL = 3;

    /**
     * Starts at 1, adds 1 when an email is present and 1 when the owner has no namesakes, capped at
     * {@link #MAX_CREATE_MEMBERSHIP_LEVEL}.
     */
    default Integer toMembershipLevel(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX_CREATE_MEMBERSHIP_LEVEL);
    }

    /**
     * Builds the membership number '&lt;customerCode&gt;-M&lt;YY&gt;', where YY is the last two digits of
     * the registration date's year.
     */
    default String toMembershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            Math.floorMod(owner.getRegistrationDate().getYear(), 100));
    }

    /**
     * The Luhn check digit over the digits of the customer code, ignoring non-digit characters.
     */
    default Integer toCheckDigit(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return null;
        }
        int sum = 0;
        boolean doubled = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubled) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubled = !doubled;
        }
        return (10 - sum % 10) % 10;
    }

    default String toInitials(Owner owner) {
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private static String initial(String name) {
        return name == null || name.isEmpty() ? "" : Character.toUpperCase(name.charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

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
