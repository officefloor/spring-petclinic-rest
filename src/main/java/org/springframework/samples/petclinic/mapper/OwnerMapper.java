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
import java.time.Period;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /**
     * Used to size the owner's household (the number of owners sharing the same
     * {@code householdId}) when scoring membership points. Field-injected so the
     * MapStruct-generated subclass, a Spring bean, receives it. May be {@code null} when the
     * mapper is used outside a Spring context; the household factor then scores nothing.
     */
    @Autowired
    protected OwnerRepository ownerRepository;

    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
                    + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey.forOwner(owner))")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneNormalizer.toDisplay(owner.getTelephone()))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Scores the owner's membership points. Starts at 0; add 2 when an email address is on
     * file; add 1 when the owner is the first with their name ({@code namesakeCount} is 0);
     * add 2 for a household of 3 or more members; add 3 once the owner's tenure spans at least
     * one full fiscal year (their registration fiscal year is before the current fiscal year).
     */
    protected Integer membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (householdSize(owner) >= 3) {
            points += 2;
        }
        if (tenureFiscalYears(owner) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level: their {@link #naturalMembershipLevel(Owner) natural}
     * level, capped so it never exceeds one above the current maximum level among their existing
     * household members (other owners sharing the same {@code householdId}). With no existing
     * household member on file no cap applies, so the natural level is returned unchanged.
     */
    protected Integer membershipLevel(Owner owner) {
        int level = naturalMembershipLevel(owner);
        Integer householdMax = householdMaxMembershipLevel(owner);
        if (householdMax == null) {
            return level;
        }
        return Math.min(level, householdMax + 1);
    }

    /**
     * Bands {@link #membershipPoints(Owner)} into the numeric membership level: 1 for 0-1
     * points, 2 for 2-3, 3 for 4-5, 4 for 6 or more. This is the owner's level before the
     * household ceiling in {@link #membershipLevel(Owner)} is applied.
     */
    private Integer naturalMembershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * The maximum {@link #naturalMembershipLevel(Owner) natural} membership level among the owner's
     * existing household members — the other, non-deleted owners sharing the same
     * {@code householdId}. Returns {@code null} when the owner has no household member on file (or
     * the repository is unavailable), signalling that no household cap applies.
     */
    private Integer householdMaxMembershipLevel(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || ownerRepository == null) {
            return null;
        }
        Integer max = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted() || !householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // the owner is not their own household member
            }
            int level = naturalMembershipLevel(existing);
            if (max == null || level > max) {
                max = level;
            }
        }
        return max;
    }

    /**
     * The number of owners sharing this owner's {@code householdId} (the persisted household
     * key derived from last name and postcode). Returns 1 when no household is on file or the
     * repository is unavailable, so the household factor scores nothing in those cases.
     */
    private long householdSize(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || ownerRepository == null) {
            return 1;
        }
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * The owner's tenure in whole elapsed fiscal years, measured from the fiscal year of their
     * {@code registrationDate} to the current fiscal year (fiscal years start on 1 July). Returns 0
     * when no registrationDate is on file (e.g. a brand-new owner before one is assigned), so such
     * an owner has zero tenure.
     */
    private int tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return org.springframework.samples.petclinic.rest.function.owner.FiscalYear
                .elapsed(registrationDate, LocalDate.now());
    }

    /**
     * The owner's {@code fiscalYear} label {@code FY<YY>}, derived from the fiscal year of the
     * business-day-adjusted {@code registrationDate} (fiscal years start on 1 July). Returns
     * {@code null} when no registrationDate is on file, so the field is then absent from the
     * response.
     */
    protected String fiscalYear(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return org.springframework.samples.petclinic.rest.function.owner.FiscalYear.label(registrationDate);
    }

    /**
     * Computes a single Luhn check digit (0-9) over the digits contained in the owner's
     * {@code customerCode}. Non-digit characters (separators) are ignored. Returns
     * {@code null} when no customer code has been assigned.
     */
    protected Integer checkDigit(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return null;
        }
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
     * Derives the owner's {@code locality} from the REGION component of the
     * {@code <REGION>-<HASH8>} customer code — the region-and-hash identity is now the single
     * source of the owner's region. Falls back to the postcode/city lookup for an owner that has
     * no customer code yet (e.g. seed data created outside the create pipeline).
     */
    protected String locality(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            return dash >= 0 ? code.substring(0, dash) : code;
        }
        return LocalityLookup.regionFor(owner.getCity(), owner.getPostcode());
    }

    /**
     * Derives the owner's {@code ownerSegment} formatted {@code <TIER>_<AREA>}. TIER is
     * 'PREMIUM' when {@link #membershipLevel(Owner)} is 3 or more, otherwise 'STANDARD'.
     * AREA is 'METRO' when the {@link #locality(Owner)} is a known region (NSW, VIC or QLD),
     * otherwise 'REGIONAL'. So one of 'PREMIUM_METRO', 'PREMIUM_REGIONAL', 'STANDARD_METRO'
     * or 'STANDARD_REGIONAL'.
     */
    protected OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String locality = locality(owner);
        boolean knownRegion = "NSW".equals(locality) || "VIC".equals(locality) || "QLD".equals(locality);
        String area = knownRegion ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /**
     * Derives the owner's {@code timezone} as the IANA name for the owner's region, using
     * the fixed region-to-timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane). The region is the same {@link #locality(Owner)} value.
     * Returns {@code null} when the region has no mapped timezone (e.g. "UNKNOWN"), so the
     * field is then absent from the response.
     */
    protected String timezone(Owner owner) {
        return LocalityLookup.timezoneFor(locality(owner));
    }

    /**
     * Derives the owner's preferred contact channel: 'EMAIL' when an email address is
     * on file, otherwise 'PHONE'.
     */
    protected String contactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }

    /**
     * Derives the owner's age band from their birthDate as of their registrationDate:
     * 'MINOR' when under 18, 'ADULT' when 18-64, 'SENIOR' when 65 or over. Returns
     * {@code null} when no birthDate is on file (the field is then absent from the
     * response). The reference date is the registrationDate, falling back to the current
     * date only for legacy owners created without one.
     */
    protected OwnerDto.AgeBandEnum ageBand(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate asOf = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int age = Period.between(birthDate, asOf).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Composes the owner's {@code salutation}: the {@code title} honorific followed by a
     * single space and the lastName (e.g. 'DR Franklin'), or just the lastName when no
     * title is on file.
     */
    protected String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
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
