package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Region;
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

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "identityKey", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's {@code selfLink}: the canonical link to the owner, formatted
     * {@code '/api/owners/' + id}. Returns {@code null} when the owner has no id (e.g. an
     * unpersisted owner), in which case the field is absent from the response.
     *
     * @param owner the owner being mapped
     * @return the owner's self link, or {@code null} when the owner has no id
     */
    default String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * Derives the owner's {@code salutation} from its optional {@code title} and its {@code lastName}:
     * the title, a single space and the last name when a title is present (e.g. {@code 'DR Franklin'}),
     * otherwise just the last name. A {@code null} or blank title yields the bare last name.
     *
     * @param owner the owner being mapped
     * @return the derived salutation
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * Derives the owner's {@code contactPreference} from the owner's own fields:
     * {@code EMAIL} when an email is present, otherwise {@code PHONE}.
     *
     * @param owner the owner being mapped
     * @return the derived contact preference
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        return hasEmail(owner) ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Reports whether the owner has a usable {@code email}: present and not blank (whitespace-only).
     * The presence of an email drives several derived fields (such as the {@code contactPreference}
     * and the {@code membershipLevel}), so the "has an email" test is written in exactly one place
     * rather than repeated at each use.
     *
     * @param owner the owner being mapped
     * @return {@code true} when the owner has a non-blank email
     */
    default boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    /**
     * Derives the owner's {@code ageBand} from its {@code birthDate}, measured against the owner's
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} for 18 to 64, and
     * {@code SENIOR} for 65 and over. Returns {@code null} when no birth date is present, in which
     * case the field is absent from the response. The age in whole years is the number of complete
     * years between the birth date and the registration date (see {@link java.time.Period}); when the
     * registration date is somehow absent the current date is used as the reference.
     *
     * @param owner the owner being mapped
     * @return the derived age band, or {@code null} when no birth date is present
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        java.time.LocalDate reference = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : java.time.LocalDate.now();
        int age = java.time.Period.between(birthDate, reference).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's {@code locality} as the {@code REGION} segment of its {@code memberId} (the
     * leading region-code prefix of the {@code '<REGION><FY><HASH8><CHK>'} identity). When no member id
     * is present (e.g. seed data) it falls back to resolving the region directly from the owner's
     * {@code postcode} and {@code city} through the shared {@link Region#code(String, String)}. Reading
     * the region from the identity keeps the locality consistent with the id an owner is issued.
     *
     * @param owner the owner being mapped
     * @return the derived locality
     */
    default String locality(Owner owner) {
        String memberId = owner.getMemberId();
        if (memberId == null) {
            return Region.code(owner.getPostcode(), owner.getCity());
        }
        return regionPrefix(memberId);
    }

    /**
     * The {@code REGION} segment of a member id {@code '<REGION><FY><HASH8><CHK>'}: the leading
     * region-code prefix it was built from (e.g. {@code 'NSW'} for {@code 'NSW261A2B3C4D5'}). The region
     * code is one of the known {@link Region} names, otherwise {@link Region#UNKNOWN_CODE}, both of which
     * the member id always begins with, so the prefix is recovered by matching those known codes rather
     * than by a separator (the format carries none). Holding the "read the region back out of the
     * identity" rule in one place keeps the derived {@link #locality(Owner) locality} consistent with the
     * id an owner is issued.
     *
     * <p>Declared {@code private} so MapStruct treats it as an internal helper of {@link #locality(Owner)}
     * rather than as an implicit {@code String}-to-{@code String} mapping method it might auto-apply to
     * unrelated string properties.
     *
     * @param memberId the member id the region is read from
     * @return the leading {@code REGION} segment of the member id
     */
    private String regionPrefix(String memberId) {
        for (Region region : Region.values()) {
            if (memberId.startsWith(region.name())) {
                return region.name();
            }
        }
        return Region.UNKNOWN_CODE;
    }

    /**
     * Derives the owner's {@code timezone} as the IANA name for its {@link #locality(Owner) locality}
     * region, resolved through the shared fixed region-to-timezone table
     * ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}) in {@link Region#timezoneForCode(String)}. Deriving it from
     * the same locality keeps the timezone consistent with the region the owner is labelled with.
     * Returns {@code null} when the locality has no known timezone (such as an {@code UNKNOWN}
     * region), in which case the field is absent from the response.
     *
     * @param owner the owner being mapped
     * @return the derived IANA timezone name, or {@code null} when the region has no known timezone
     */
    default String timezone(Owner owner) {
        return Region.timezoneForCode(locality(owner));
    }

    /**
     * Derives the owner's {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'}. {@code TIER} is
     * {@code PREMIUM} when the owner's {@link #membershipLevel(Owner) membershipLevel} is {@code 3} or
     * more, otherwise {@code STANDARD}. {@code AREA} is {@code METRO} when the owner's
     * {@link #locality(Owner) locality} is a known region (NSW, VIC or QLD), otherwise
     * {@code REGIONAL}. Deriving both parts from the same locality and membership level keeps the
     * segment consistent with those fields.
     *
     * @param owner the owner being mapped
     * @return the derived owner segment
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        return ownerSegment(owner, membershipLevel(owner));
    }

    /**
     * Derives the owner's {@code ownerSegment} for a given reported {@code membershipLevel}, so a
     * caller that has shaped the level with cross-owner context (such as the controller's household
     * cap) can keep the segment's {@code TIER} consistent with the level it reports. {@code TIER} is
     * {@code PREMIUM} when {@code membershipLevel} is {@code 3} or more, otherwise {@code STANDARD};
     * {@code AREA} is {@code METRO} when the owner's {@link #locality(Owner) locality} is a known
     * region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
     *
     * @param owner the owner being mapped
     * @param membershipLevel the membership level to base the tier on
     * @return the derived owner segment
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner, int membershipLevel) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = Region.forCode(locality(owner)).isPresent() ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /**
     * Derives the owner's {@code membershipPoints} from the owner's factors. Starts at {@code 0},
     * adds {@code 2} when an email is present, adds {@code 1} when the owner's {@code namesakeCount}
     * is {@code 0}, adds {@code 2} for a household of {@code 3} or more (see
     * {@link Owner#getHouseholdSize()}), and adds {@code 3} when the owner's tenure &mdash; the number
     * of elapsed fiscal years between its {@code registrationDate} and the current date &mdash; exceeds
     * one fiscal year (see {@link #tenureFiscalYears(Owner)}).
     *
     * @param owner the owner being mapped
     * @return the derived membership points
     */
    default Integer membershipPoints(Owner owner) {
        int points = 0;
        if (hasEmail(owner)) {
            points += 2;
        }
        boolean uniqueName = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (uniqueName) {
            points += 1;
        }
        if (owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (tenureFiscalYears(owner) > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Maps the owner's {@code membershipPoints} to its numeric {@code membershipLevel}: level
     * {@code 1} for {@code 0-1} points, {@code 2} for {@code 2-3}, {@code 3} for {@code 4-5}, and
     * {@code 4} for {@code 6} or more points.
     *
     * @param owner the owner being mapped
     * @return the derived membership level, between {@code 1} and {@code 4}
     */
    default Integer membershipLevel(Owner owner) {
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
     * Computes the owner's tenure as the number of elapsed fiscal years between the owner's
     * {@code registrationDate} and the current date: the fiscal year of "now" minus the fiscal year
     * of the registration date (see {@link #fiscalYearNumber(java.time.LocalDate)}). An owner
     * registered and read within the same fiscal year has a tenure of {@code 0}; each subsequent
     * fiscal year that has started adds one. Returns {@code 0} when no registration date is present,
     * so an owner without a registration date has no tenure.
     *
     * @param owner the owner being mapped
     * @return the owner's tenure in elapsed fiscal years, or {@code 0} when no registration date is present
     */
    default long tenureFiscalYears(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return (long) fiscalYearNumber(java.time.LocalDate.now()) - fiscalYearNumber(registrationDate);
    }

    /**
     * Computes the calendar year that identifies the fiscal year containing {@code date}. The fiscal
     * year starts on 1 July and is labelled by the calendar year in which it starts, so a date on or
     * after 1 July belongs to that same year's fiscal year, while a date before 1 July belongs to the
     * previous year's fiscal year (e.g. {@code 2026-09-06} yields {@code 2026} and {@code 2026-06-15}
     * yields {@code 2025}).
     *
     * @param date the date whose fiscal year is wanted
     * @return the calendar year the fiscal year starts in
     */
    default int fiscalYearNumber(java.time.LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue() ? date.getYear() : date.getYear() - 1;
    }

    /**
     * Derives the owner's {@code fiscalYear}, formatted {@code 'FY<YY>'}, where {@code YY} is the
     * {@code FY} segment of the owner's {@code memberId} (the two-digit fiscal year of the
     * business-day-adjusted {@code registrationDate} baked into the id, e.g. {@code 'FY26'}). When no
     * member id is present (e.g. seed data) it falls back to the segment derived directly from the
     * {@code registrationDate}. Returns {@code null} when neither is available.
     *
     * @param owner the owner being mapped
     * @return the formatted fiscal year, or {@code null} when it cannot be derived
     */
    default String fiscalYear(Owner owner) {
        String segment = memberIdFiscalYearSegment(owner);
        return segment == null ? null : "FY" + segment;
    }

    /**
     * The two-digit {@code FY} segment of the owner's {@code fiscalYear}, read from the owner's
     * {@code memberId} when present so the reported fiscal year references the id an owner is issued: it
     * is the two characters immediately following the id's {@linkplain #regionPrefix(String) region
     * prefix}. When no member id is present (e.g. seed data) it falls back to the segment derived
     * directly from the {@code registrationDate} ({@link #fiscalYearSegment(Owner)}). Returns
     * {@code null} when neither is available.
     *
     * @param owner the owner being mapped
     * @return the two-digit fiscal-year segment, or {@code null} when it cannot be derived
     */
    default String memberIdFiscalYearSegment(Owner owner) {
        String memberId = owner.getMemberId();
        if (memberId == null) {
            return fiscalYearSegment(owner);
        }
        int start = regionPrefix(memberId).length();
        return memberId.substring(start, start + 2);
    }

    /**
     * The two-digit {@code YY} fiscal-year segment of the owner's business-day-adjusted
     * {@code registrationDate}: the last two digits of its
     * {@linkplain #fiscalYearNumber(java.time.LocalDate) fiscal year}, zero-padded (e.g. {@code '26'}).
     * This is the {@code YY} baked into the {@code memberId} as its {@code FY} segment when the id is
     * built, and the fallback used by {@link #memberIdFiscalYearSegment(Owner)} for owners without a
     * member id, so the fiscal year derives identically either way. Returns {@code null} when no
     * registration date is present.
     *
     * @param owner the owner being mapped
     * @return the two-digit fiscal-year segment, or {@code null} when no registration date is present
     */
    default String fiscalYearSegment(Owner owner) {
        if (owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%02d", fiscalYearNumber(owner.getRegistrationDate()) % 100);
    }

    /**
     * Formats the owner's stored E.164 {@code telephone} for human display: the country calling
     * code, a space, then the national-significant digits grouped in threes separated by spaces
     * (e.g. {@code '+61412345678'} becomes {@code '+61 412 345 678'}). The raw {@code telephone}
     * field keeps the unformatted E.164 value. The split between the country code and the national
     * digits mirrors the recognised calling codes of the {@code TelephoneNormalizer}: Australian
     * {@code +61} (9 national digits) and North American {@code +1} (10); an unrecognised or
     * malformed value falls back to a single leading digit. Returns the value unchanged when it is
     * {@code null}, blank, or not in E.164 form.
     *
     * @param owner the owner being mapped
     * @return the human-formatted telephone, or the raw value when it cannot be formatted
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || telephone.isBlank() || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int codeLength = (digits.startsWith("61") && digits.length() == 2 + 9) ? 2 : 1;
        String countryCode = digits.substring(0, codeLength);
        String national = digits.substring(codeLength);
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + countryCode + " " + grouped;
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
