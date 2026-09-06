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
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
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
     * The owner's {@code membershipPoints}: the raw loyalty score (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.Memberships}), built from a
     * present email, no namesakes, a household of 3 or more and tenure over 365 days.
     */
    protected int membershipPoints(Owner owner) {
        long householdSize = org.springframework.samples.petclinic.rest.function.owner.Households
                .size(owner, ownerRepository);
        return org.springframework.samples.petclinic.rest.function.owner.Memberships
                .membershipPoints(owner, householdSize);
    }

    /**
     * The owner's {@code membershipLevel}: the grade {@code membershipPoints} buckets into — 1 for
     * 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more.
     */
    protected int membershipLevel(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.Memberships
                .membershipLevel(membershipPoints(owner));
    }

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
