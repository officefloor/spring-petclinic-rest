package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's {@code ageBand} from its birthDate relative to the registrationDate:
     * {@code MINOR} when under 18, {@code ADULT} when 18 to 64, and {@code SENIOR} when 65 or older.
     * Returns {@code null} when the owner has no birthDate or no registrationDate.
     */
    default OwnerDto.@Nullable AgeBandEnum ageBand(@Nullable Owner owner) {
        if (owner == null || owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's duplicate-detection {@code identityKey}, formatted as
     * {@code normalizedTelephone + '|' + email + '|' + householdId} with an empty email segment when
     * the owner has no email. This is the same key the create endpoint uses to reject an owner whose
     * whole key equals an existing owner's.
     */
    default @Nullable String identityKey(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email is present,
     * otherwise {@code PHONE}.
     */
    default OwnerDto.@Nullable ContactPreferenceEnum contactPreference(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's locality from its region-and-hash identity: the {@code REGION} prefix of
     * the {@code customerCode} ({@code '<REGION>-<HASH8>'}), which is the region code derived from
     * the postcode on create. Returns the canonical region string, or {@code 'UNKNOWN'} when the
     * owner has no customerCode.
     */
    default @Nullable String locality(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        String customerCode = owner.getCustomerCode();
        if (customerCode == null) {
            return "UNKNOWN";
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? "UNKNOWN" : customerCode.substring(0, dash);
    }

    /**
     * Returns the owner's numeric membership level from 1 to 4. Starting at 1, it adds 1 when an
     * email is present, 1 when namesakeCount is 0 and 1 when the owner belongs to a household of at
     * least 3 members. Level 4 additionally requires tenure of more than 365 days since the
     * registrationDate; without that tenure the level is capped at 3. Because a newly created owner
     * has zero tenure it never exceeds level 3 (so a new owner with an email, a namesakeCount of 0
     * and a 3-member household is level 3, not 4).
     */
    default @Nullable Integer membershipLevel(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        if (hasEmail) {
            level++;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            level++;
        }
        boolean largeHousehold = owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3;
        if (largeHousehold) {
            level++;
        }
        int cap = hasQualifyingTenure(owner) ? 4 : 3;
        return Math.min(level, cap);
    }

    /**
     * Whether the owner's tenure exceeds 365 days, i.e. more than a year has elapsed between its
     * registrationDate and the current date. Returns {@code false} when the registrationDate is
     * absent (or in the future), so a newly created owner has no qualifying tenure.
     */
    default boolean hasQualifyingTenure(@Nullable Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return false;
        }
        long tenureDays = java.time.temporal.ChronoUnit.DAYS.between(
            owner.getRegistrationDate(), java.time.LocalDate.now());
        return tenureDays > 365;
    }

    /**
     * Formats an owner's membership number as {@code '<customerCode>-M<YY>'}, where YY is the
     * last two digits of the registrationDate year, e.g. {@code 'MEL-SMI-0007-M26'}.
     */
    default @Nullable String membershipNumber(@Nullable Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Returns the Luhn check digit (0-9) computed over the digits contained in the owner's
     * customerCode, or {@code null} when the owner or its customerCode is absent.
     */
    default @Nullable Integer checkDigit(@Nullable Owner owner) {
        if (owner == null || owner.getCustomerCode() == null) {
            return null;
        }
        String s = owner.getCustomerCode();
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Formats the owner's stored E.164 {@code telephone} for humans: the country code, a space, then
     * the national digits grouped in threes separated by spaces, e.g. {@code '+61 412 345 678'} for a
     * stored {@code '+61412345678'}. The country code is the single digit for the {@code +1} and
     * {@code +7} plans and two digits otherwise. Returns the raw value unchanged when it is not a
     * {@code '+'}-prefixed digit string, or {@code null} when absent.
     */
    default @Nullable String telephoneDisplay(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.matches("\\+\\d+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int countryCodeLength = (digits.startsWith("1") || digits.startsWith("7")) ? 1 : 2;
        countryCodeLength = Math.min(countryCodeLength, digits.length());
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'}.
     */
    default @Nullable String displayName(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    default @Nullable String initials(@Nullable Owner owner) {
        if (owner == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String firstName = owner.getFirstName();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(Character.toUpperCase(firstName.charAt(0))).append('.');
        }
        String lastName = owner.getLastName();
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(Character.toUpperCase(lastName.charAt(0))).append('.');
        }
        return sb.toString();
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
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
