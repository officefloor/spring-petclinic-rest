package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /**
     * More than this many owners already sharing a {@code registrationDate} raises the
     * {@code bulkSignupWarning} on responses for that day.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    @Autowired
    protected OwnerRepository ownerRepository;

    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(owner))")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.Telephones.toDisplay(owner.getTelephone()))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's preferred contact channel: {@code EMAIL} when an email address is
     * present (non-null and non-blank), otherwise {@code PHONE}.
     */
    protected OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /** The age (in years) at which an owner stops being a minor and becomes an adult. */
    private static final int ADULT_AGE = 18;

    /** The age (in years) at which an owner becomes a senior. */
    private static final int SENIOR_AGE = 65;

    /**
     * Derives the owner's {@code ageBand} from {@code birthDate} as of the
     * {@code registrationDate}: {@code MINOR} when under {@value #ADULT_AGE},
     * {@code ADULT} when {@value #ADULT_AGE} to {@value #SENIOR_AGE}-1, and
     * {@code SENIOR} when {@value #SENIOR_AGE} or older. Returns {@code null} when
     * either the birth date or the registration date is absent, so an owner created
     * without a birth date has no age band.
     */
    protected OwnerDto.AgeBandEnum ageBand(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(birthDate, registrationDate).getYears();
        if (age < ADULT_AGE) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < SENIOR_AGE) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's {@code locality} (canonical region string) from the
     * region-and-hash identity: the REGION segment of the {@code customerCode} (everything
     * before the first {@code '-'}). Legacy owners with no customer code (e.g. seed data)
     * fall back to deriving the region directly from postcode, then city, then
     * {@code "UNKNOWN"}.
     */
    protected String locality(Owner owner) {
        String region = org.springframework.samples.petclinic.rest.function.owner.CustomerCodes
                .regionOf(owner.getCustomerCode());
        if (region != null) {
            return region;
        }
        return org.springframework.samples.petclinic.rest.function.owner.CustomerCodes.region(owner);
    }

    /** The top membership level, reachable only with tenure. */
    private static final int MAX_MEMBERSHIP_LEVEL = 4;

    /**
     * The highest level reachable from the pre-tenure factors alone; the top level
     * ({@value #MAX_MEMBERSHIP_LEVEL}) is unlocked only by tenure.
     */
    private static final int PRE_TENURE_MAX_LEVEL = MAX_MEMBERSHIP_LEVEL - 1;

    /**
     * Tenure, in days since {@code registrationDate}, that must be exceeded to reach the
     * top membership level.
     */
    private static final int TENURE_DAYS_FOR_TOP_LEVEL = 365;

    /**
     * Returns the owner's membership level, a number from 1 to
     * {@value #MAX_MEMBERSHIP_LEVEL}: it starts at 1, gains 1 when an email address is
     * present, gains 1 when the owner has no namesakes (namesakeCount is 0), and these
     * pre-tenure factors are capped at {@value #PRE_TENURE_MAX_LEVEL}. Level
     * {@value #MAX_MEMBERSHIP_LEVEL} is reserved for tenure: it is reached only when the
     * owner has more than {@value #TENURE_DAYS_FOR_TOP_LEVEL} days of tenure. A newly
     * created owner registers as of the current day and so has zero tenure, meaning a new
     * owner never exceeds level {@value #PRE_TENURE_MAX_LEVEL}.
     */
    public static int membershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            level++;
        }
        // Level 4 is reserved for tenure: cap the pre-tenure factors, then let more than
        // TENURE_DAYS_FOR_TOP_LEVEL days of tenure unlock the top level.
        level = Math.min(level, PRE_TENURE_MAX_LEVEL);
        if (hasTopLevelTenure(owner)) {
            level++;
        }
        return level;
    }

    /**
     * True when the owner's tenure (days between {@code registrationDate} and today)
     * exceeds {@value #TENURE_DAYS_FOR_TOP_LEVEL}. An owner with no registration date
     * (e.g. legacy seed owners) has no tenure.
     */
    private static boolean hasTopLevelTenure(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long tenureDays = java.time.temporal.ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
        return tenureDays > TENURE_DAYS_FOR_TOP_LEVEL;
    }

    /**
     * Builds the membership number '<customerCode>-M<YY>', where YY is the last two digits of
     * the registrationDate year (e.g. 'NSW-1A2B3C4D-M26'). Returns {@code null} when either the
     * customer code or the registration date is absent (e.g. legacy seed owners).
     */
    protected String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * True when more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} other owners have already
     * been created for this owner's {@code registrationDate}, using the same per-day
     * accumulation the daily create limit enforces (the owner itself is excluded). Returns
     * {@code false} when the owner has no registration date (e.g. legacy seed owners).
     */
    protected boolean bulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long count = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
                .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * The Luhn check digit (0-9) computed over the digits contained in the owner's
     * {@code customerCode}. Returns {@code null} when the customer code is absent (e.g.
     * legacy seed owners).
     */
    protected Integer checkDigit(Owner owner) {
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

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

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
