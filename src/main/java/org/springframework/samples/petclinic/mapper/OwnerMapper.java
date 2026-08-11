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

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "membershipNumber",
        expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(locality(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey",
        expression = "java(identityKey(owner))")
    @Mapping(target = "checkDigit",
        expression = "java(checkDigit(owner))")
    @Mapping(target = "ageBand",
        expression = "java(ageBand(owner))")
    @Mapping(target = "sharesHousehold", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derive the owner's age band from the supplied birthDate, evaluated against the owner's
     * registrationDate: 'MINOR' when under 18, 'ADULT' when 18 to 64 inclusive, and 'SENIOR' when
     * 65 or older. Returns null when no birthDate was supplied (or no registrationDate is available)
     * so owners without a birth date serialize cleanly.
     */
    default String ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The E.164 country calling codes whose split from the national number is recognised when
     * formatting {@code telephoneDisplay} (matching the codes validated on create). The longest
     * matching code wins so a shorter code that is a prefix of another cannot mask it.
     */
    java.util.Set<String> KNOWN_COUNTRY_CODES = java.util.Set.of("1", "61");

    /**
     * Format the stored E.164 telephone for humans: the '+' and country calling code, a space, then
     * the national digits grouped in threes (e.g. '+61412345678' -&gt; '+61 412 345 678'). The
     * country code is split off using the recognised calling codes; when none matches, a single-digit
     * code is assumed so the value is still rendered as '+&lt;code&gt; &lt;grouped national&gt;'.
     * Returns the stored value unchanged when it is null or not in E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (!digits.matches("\\d+")) {
            return telephone;
        }
        String countryCode = digits.substring(0, 1);
        String national = digits.substring(1);
        for (int codeLength = Math.min(3, digits.length() - 1); codeLength >= 1; codeLength--) {
            String candidate = digits.substring(0, codeLength);
            if (KNOWN_COUNTRY_CODES.contains(candidate)) {
                countryCode = candidate;
                national = digits.substring(codeLength);
                break;
            }
        }
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + countryCode + " " + grouped;
    }

    /**
     * Compute the owner's check digit: a single Luhn check digit (0-9) over the digits contained in
     * the customerCode. Returns null when no customerCode has been assigned so owners without a code
     * serialize cleanly.
     */
    default Integer checkDigit(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
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
     * Derive the owner's identity key, the single value into which all duplicate detection is
     * consolidated: {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Two
     * owners are duplicates only when their whole identity keys are equal. A null email or
     * householdId contributes an empty segment.
     */
    default String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Determine the owner's preferred contact channel: 'EMAIL' when an email address is present,
     * otherwise 'PHONE'.
     */
    default String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Build the owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is
     * the last two digits of the registrationDate year. Returns null when either source field is
     * absent so owners without an assigned code or registration date serialize cleanly.
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Determine the owner's numeric membership level. Starts at 1; add 1 when an email address is
     * present; add 1 when the owner has no namesakes (namesakeCount is 0). These pre-tenure factors
     * are capped at 3. Level 4 is a loyalty tier reserved for tenure of more than 365 days, measured
     * from the registrationDate to the current date; because a newly created owner has zero tenure,
     * a new owner never exceeds level 3.
     */
    default Integer membershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        if (hasEmail) {
            level++;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            level++;
        }
        // The pre-tenure factors above are capped at 3. Level 4 is the tenure loyalty tier: it
        // requires tenure of more than 365 days measured from the registrationDate. A new owner is
        // registered on the current date, so its tenure is zero and it can never reach level 4.
        level = Math.min(level, 3);
        if (owner.getRegistrationDate() != null && java.time.temporal.ChronoUnit.DAYS.between(
                owner.getRegistrationDate(), java.time.LocalDate.now()) > 365) {
            level++;
        }
        return level;
    }

    /**
     * The fixed city-to-region table used to derive an owner's locality when the postcode is absent
     * or falls in no known range.
     */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Region -&gt; inclusive 4-digit postcode range {low, high}: NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099.
     */
    java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derive the owner's locality (canonical region), preferring the postcode: look up the region by
     * postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only fall back to the
     * city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD) when the postcode is
     * absent or in no known range. Returns 'UNKNOWN' when neither source resolves a region.
     */
    default String locality(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("\\d{4}")) {
            int value = Integer.parseInt(postcode);
            for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        String city = owner.getCity();
        if (city == null) {
            return "UNKNOWN";
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
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
