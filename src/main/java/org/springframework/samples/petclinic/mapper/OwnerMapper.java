package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.controller.CityRegionResolver;
import org.springframework.samples.petclinic.rest.controller.TelephoneNormalizer;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.ClinicService;

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

    /** Used to count the members of an owner's household when deriving derived fields. */
    @Autowired
    protected ClinicService clinicService;

    /** Single source of truth for the city-to-region mapping behind an owner's locality. */
    @Autowired
    protected CityRegionResolver cityRegionResolver;

    /** Formats the stored E.164 telephone into its human-readable display form. */
    @Autowired
    protected TelephoneNormalizer telephoneNormalizer;

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneNormalizer.toDisplayForm(owner.getTelephone()))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's age band derived from {@code birthDate} relative to the
     * {@code registrationDate}: 'MINOR' when under 18, 'ADULT' from 18 to 64
     * inclusive and 'SENIOR' at 65 or older. Returns null when either date is
     * absent so no band can be computed.
     */
    OwnerDto.AgeBandEnum ageBand(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * The owner's check digit: a single Luhn check digit (0-9) computed over the digits
     * contained in the customerCode, or null when no customerCode has been assigned.
     */
    Integer checkDigit(Owner owner) {
        String customerCode = owner.getCustomerCode();
        return customerCode == null ? null : luhn(customerCode);
    }

    /**
     * The Luhn check digit (0-9) over the digits contained in {@code s}: from the rightmost
     * digit leftward, every second digit is doubled (the rightmost first) with digits over 9
     * reduced by 9, and the check digit makes the total a multiple of ten.
     */
    static int luhn(String s) {
        int sum = 0;
        boolean doubling = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubling) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubling = !doubling;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * The owner's derived identity key: the normalized telephone, the email (or an empty
     * string when absent) and the householdId joined by '|'. This single key is the sole
     * basis for duplicate detection on create — two owners are duplicates only when their
     * whole identityKey matches, so members of one household with different telephones
     * (and hence different keys) are all permitted.
     */
    public static String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * The owner's preferred contact channel: 'EMAIL' when an email address is
     * present, otherwise 'PHONE'.
     */
    String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's locality: the REGION component of the customerCode identity (the part before
     * the first '-' of {@code '<REGION>-<HASH8>'}). When no customerCode has been assigned this
     * falls back to the canonical region derived from the owner via {@link CityRegionResolver},
     * preferring the postcode over the city, or 'UNKNOWN' when neither yields a known region.
     */
    String locality(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            if (dash >= 0) {
                return customerCode.substring(0, dash);
            }
        }
        return cityRegionResolver.regionFor(owner.getCity(), owner.getPostcode());
    }

    /**
     * The owner's membership points: starting at 0, plus 2 when an email address is present,
     * plus 1 when namesakeCount is 0, plus 2 for a household of 3 or more members, plus 3 when
     * the owner's tenure is more than 365 days.
     */
    public int membershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean uniqueName = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (uniqueName) {
            points += 1;
        }
        if (householdSize(owner) >= 3) {
            points += 2;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        boolean tenured = registrationDate != null
            && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > 365;
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level from 1 to 4, derived from {@link #membershipPoints}:
     * level 1 for 0-1 points, level 2 for 2-3 points, level 3 for 4-5 points and level 4 for
     * 6 or more points.
     */
    public int membershipLevel(Owner owner) {
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
     * The number of owners belonging to {@code owner}'s household, i.e. those sharing its
     * householdId (including the owner itself once persisted). Returns 0 when the owner has no
     * household id assigned.
     */
    int householdSize(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 0;
        }
        return (int) clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
    }

    /**
     * The upper-cased first letters of firstName and lastName, dot-separated with a
     * trailing dot, e.g. 'J.S.'.
     */
    String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
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
