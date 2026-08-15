package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /**
     * Points added when the owner has an email address.
     */
    private static final int EMAIL_POINTS = 2;

    /**
     * Points added when the owner has no namesakes ({@code namesakeCount} is 0).
     */
    private static final int NO_NAMESAKE_POINTS = 1;

    /**
     * Points added when the owner belongs to a household of {@value #LARGE_HOUSEHOLD_SIZE} or more members.
     */
    private static final int LARGE_HOUSEHOLD_POINTS = 2;

    /**
     * The household size (members, including the owner) at which the large-household points are awarded.
     */
    private static final int LARGE_HOUSEHOLD_SIZE = 3;

    /**
     * Points added when the owner's tenure exceeds {@value #TENURE_POINTS_DAYS} days.
     */
    private static final int TENURE_POINTS = 3;

    /**
     * Tenure, in days, that an owner must exceed to earn the tenure points.
     */
    private static final int TENURE_POINTS_DAYS = 365;

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.of(owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Computes an owner's membership points. Points start at 0 and accumulate: {@value #EMAIL_POINTS} when an
     * email is present, {@value #NO_NAMESAKE_POINTS} when {@code namesakeCount} is 0,
     * {@value #LARGE_HOUSEHOLD_POINTS} for a household of {@value #LARGE_HOUSEHOLD_SIZE} or more members, and
     * {@value #TENURE_POINTS} for tenure of more than {@value #TENURE_POINTS_DAYS} days (measured from
     * {@code registrationDate}). Because a newly created owner has zero tenure, a new owner never earns the
     * tenure points.
     *
     * @param owner the owner whose points are being computed
     * @return the owner's membership points (0 or more)
     */
    protected Integer membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += EMAIL_POINTS;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += NO_NAMESAKE_POINTS;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= LARGE_HOUSEHOLD_SIZE) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (tenureDays(owner) > TENURE_POINTS_DAYS) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * Maps an owner's membership points to a numeric membership level: level 1 for 0-1 points, level 2 for
     * 2-3 points, level 3 for 4-5 points, and level 4 for 6 or more points.
     *
     * @param owner the owner whose level is being computed
     * @return the membership level (between 1 and 4 inclusive)
     */
    protected Integer membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }

    /**
     * Computes an owner's tenure in days: the number of days between {@code registrationDate} and today. A
     * newly created owner (registered today) has zero tenure. Owners with no {@code registrationDate}, or a
     * registration date in the future, are treated as having zero tenure.
     *
     * @param owner the owner whose tenure is being computed
     * @return the owner's tenure in days, never negative
     */
    protected long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
        return Math.max(days, 0);
    }

    /**
     * Computes the owner's {@code checkDigit}: a single Luhn check digit (0-9) over the digits contained in
     * the owner's {@code customerCode}. Non-digit characters (such as the letters and dashes in the code) are
     * skipped; the rightmost digit is doubled and every second digit thereafter, digits exceeding 9 after
     * doubling have 9 subtracted, and the check digit is {@code (10 - sum % 10) % 10}.
     *
     * @param owner the owner whose check digit is being computed
     * @return the Luhn check digit (between 0 and 9 inclusive) over the customer code's digits
     */
    protected Integer checkDigit(Owner owner) {
        String code = owner.getCustomerCode() == null ? "" : owner.getCustomerCode();
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
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
     * Derives an owner's preferred contact channel at read time: {@code 'EMAIL'} when an email address is
     * present, otherwise {@code 'PHONE'}.
     *
     * @param owner the owner whose contact preference is being computed
     * @return {@code 'EMAIL'} when the owner has a non-blank email, otherwise {@code 'PHONE'}
     */
    protected String contactPreference(Owner owner) {
        return (owner.getEmail() != null && !owner.getEmail().isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * Derives an owner's age band from {@code birthDate}, measured against {@code registrationDate}: the
     * completed years between the two dates place the owner in {@code 'MINOR'} (under 18), {@code 'ADULT'}
     * (18 to 64 inclusive) or {@code 'SENIOR'} (65 or over). Returns {@code null} when the owner has no
     * {@code birthDate}, so the field is simply absent for owners created without one.
     *
     * @param owner the owner whose age band is being derived
     * @return {@code 'MINOR'}, {@code 'ADULT'} or {@code 'SENIOR'}, or {@code null} when no birth date is set
     */
    protected String ageBand(Owner owner) {
        if (owner.getBirthDate() == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int years = Period.between(owner.getBirthDate(), reference).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * Derives an owner's {@code identityKey}: the normalized telephone, the email (or an empty string when
     * absent) and the household id (or an empty string when absent), joined by {@code '|'} in that order
     * (e.g. {@code '+61412345678||a1b2c3d4e5f6a7b8'}). This is the single key used for duplicate detection,
     * so the value returned here matches the one the create endpoint compares.
     *
     * @param owner the owner whose identity key is being derived
     * @return the owner's identity key
     */
    protected String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Formats the owner's stored E.164 {@code telephone} for humans: the country code, a space, then the
     * national digits grouped in threes from the left (e.g. {@code '+61412345678'} becomes
     * {@code '+61 412 345 678'}). The raw {@code telephone} is left untouched in E.164 form. Values that are
     * {@code null} or not in E.164 form (no leading {@code '+'}) are returned unchanged.
     * <p>
     * The country code is split off using the codes the application recognises: {@code '+1'} (NANP) is a
     * single digit and any other number is assumed to carry the default {@code '+61'}-style two-digit code.
     *
     * @param owner the owner whose telephone is being formatted
     * @return the human-formatted telephone, or the raw value when it is {@code null} or not E.164
     */
    protected String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int countryCodeLength = digits.startsWith("1") ? 1 : 2;
        if (digits.length() <= countryCodeLength) {
            return telephone;
        }
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder grouped = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i++) {
            if (i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return grouped.toString();
    }

    public OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
