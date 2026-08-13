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

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner &amp; OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    @Autowired
    protected OwnerRepository ownerRepository;

    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "salutation",
            expression = "java(salutation(owner))")
    @Mapping(target = "membershipPoints",
            expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.Localities.regionOf(owner.getCustomerCode()))")
    @Mapping(target = "timezone",
            expression = "java(org.springframework.samples.petclinic.rest.function.common.Localities.timezoneOf(owner.getCustomerCode()))")
    @Mapping(target = "contactPreference",
            expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey.of(owner))")
    @Mapping(target = "checkDigit",
            expression = "java(checkDigit(owner))")
    @Mapping(target = "telephoneDisplay",
            expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "ageBand",
            expression = "java(ageBand(owner))")
    @Mapping(target = "fiscalYear",
            expression = "java(fiscalYear(owner))")
    @Mapping(target = "selfLink",
            expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    public abstract OwnerDto toOwnerDto(Owner owner);

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

    /**
     * Compose the owner's salutation: the stored title, a single space and the last name when a
     * title was supplied, or just the last name when no title is given.
     */
    protected String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Score the owner's membership points. Starts at 0, plus 2 when an email is present, plus 1
     * when {@code namesakeCount} is 0, plus 2 for a household of 3 or more owners, plus 3 when
     * tenure spans at least one elapsed fiscal year (fiscal years start 1 July). Because a newly
     * created owner registers within the current fiscal year it has zero tenure, so the tenure points
     * are only earned on later reads.
     */
    protected Integer membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount().intValue() == 0) {
            points += 1;
        }
        if (householdSize(owner) >= 3) {
            points += 2;
        }
        if (owner.getRegistrationDate() != null
                && org.springframework.samples.petclinic.rest.function.owner.FiscalYear.of(java.time.LocalDate.now())
                        - org.springframework.samples.petclinic.rest.function.owner.FiscalYear
                                .of(owner.getRegistrationDate()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Map {@link #membershipPoints(Owner)} to a membership level from 1 to 4: level 1 for 0-1
     * points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more.
     */
    protected Integer membershipLevel(Owner owner) {
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
     * The number of owners in this owner's household, identified by the shared {@code householdId}
     * assigned at creation. An owner with no householdId counts only as itself.
     */
    protected long householdSize(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
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
     * Derive the owner's preferred contact channel: {@code EMAIL} when an email is present,
     * otherwise {@code PHONE}.
     */
    protected String contactPreference(Owner owner) {
        return (owner.getEmail() != null && !owner.getEmail().isEmpty()) ? "EMAIL" : "PHONE";
    }

    /**
     * Derive the owner's age band from {@code birthDate} as at {@code registrationDate}:
     * {@code MINOR} when under 18, {@code ADULT} from 18 to 64, {@code SENIOR} at 65 or over.
     * Returns {@code null} when no birth date was supplied.
     */
    protected String ageBand(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The owner's fiscal year, formatted {@code FY<YY>} (last two digits) and derived from the
     * business-day-adjusted {@code registrationDate}; the fiscal year starts on 1 July. Returns
     * {@code null} when no registration date is set.
     */
    protected String fiscalYear(Owner owner) {
        return owner.getRegistrationDate() == null ? null
                : org.springframework.samples.petclinic.rest.function.owner.FiscalYear
                        .label(owner.getRegistrationDate());
    }

    /**
     * Known country codes (digits after the {@code '+'}), longest first so {@code '61'} is
     * matched before {@code '1'}, mirroring {@code NormalizeOwnerTelephone}.
     */
    private static final String[] COUNTRY_CODES = {"61", "1"};

    /**
     * Format the stored E.164 telephone for humans: the country code, a space, then the national
     * digits grouped in threes from the left (e.g. {@code +61412345678} -> {@code +61 412 345 678}).
     * Returns the raw value unchanged when it is not a recognised E.164 number.
     */
    protected String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String code = null;
        for (String candidate : COUNTRY_CODES) {
            if (digits.startsWith(candidate)) {
                code = candidate;
                break;
            }
        }
        if (code == null) {
            return telephone;
        }
        String national = digits.substring(code.length());
        StringBuilder grouped = new StringBuilder("+").append(code);
        for (int i = 0; i < national.length(); i += 3) {
            grouped.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return grouped.toString();
    }

    /**
     * Compute a single Luhn check digit (0-9) over the digits contained in the owner's
     * {@code customerCode}. Non-digit characters (such as the hyphen separators) are ignored.
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
}
