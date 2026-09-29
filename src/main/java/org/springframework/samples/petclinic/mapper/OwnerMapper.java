package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.MembershipLevels;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.IdentityKeyNormalizer;
import org.springframework.samples.petclinic.rest.validation.IdentityVersion;
import org.springframework.samples.petclinic.rest.validation.Regions;
import org.springframework.samples.petclinic.rest.validation.RegistrationDateNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "capacityWarning", expression = "java(capacityWarning(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(possibleDuplicate(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "apiVersion", expression = "java(apiVersion())")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'}.
     */
    default String displayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String firstName = owner.getFirstName() == null ? "" : owner.getFirstName();
        return lastName + ", " + firstName;
    }

    /**
     * Composes the owner's salutation as {@code '<title> <lastName>'} — the title, a single
     * space and the last name — or just the last name when no (blank/absent) title is given.
     */
    default String salutation(Owner owner) {
        if (owner == null) {
            return null;
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Returns the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    default String initials(Owner owner) {
        if (owner == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(Character.toUpperCase(firstName.charAt(0))).append('.');
        }
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(Character.toUpperCase(lastName.charAt(0))).append('.');
        }
        return sb.toString();
    }

    /**
     * Formats the owner's stored E.164 {@code telephone} for human display: the country code,
     * a space, and the national digits grouped in threes separated by spaces, e.g.
     * {@code '+61412345678'} becomes {@code '+61 412 345 678'}. The raw {@code telephone} is
     * left unchanged in E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        return TelephoneNormalizer.toDisplay(owner.getTelephone());
    }

    /**
     * Derives the owner's {@code fiscalYear}, formatted {@code 'FY<YY>'}: the literal {@code 'FY'}
     * and the last two digits of the fiscal year derived from the (business-day-adjusted)
     * registrationDate. The fiscal year starts on 1 July. Returns {@code null} when no registration
     * date is present.
     */
    default String fiscalYear(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return "FY" + RegistrationDateNormalizer.fiscalYearYY(owner.getRegistrationDate());
    }

    /**
     * The owner's membership points; see {@link MembershipLevels#points(Owner)}.
     */
    default Integer membershipPoints(Owner owner) {
        return MembershipLevels.points(owner);
    }

    /**
     * The owner's capped numeric membership level; see {@link MembershipLevels#level(Owner)}.
     */
    default Integer membershipLevel(Owner owner) {
        return MembershipLevels.level(owner);
    }

    /**
     * Derives the owner's user-facing locality (region) directly from the owner's postcode, using
     * the shared {@link Regions#forPostcode(String)} table. This is the plain region code (e.g.
     * {@code NSW}); it is deliberately <em>not</em> read back from the {@code memberId}, whose region
     * component now carries the version-2 tag. Returns {@link Regions#UNKNOWN} when the postcode is
     * absent or in no known region.
     */
    default String locality(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Regions.forPostcode(owner.getPostcode());
    }

    /**
     * Derives the owner's IANA {@code timezone} from its locality/region using the fixed
     * region-to-timezone table ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}). Returns {@code null} when the region is not in the table.
     */
    default String timezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = locality(owner);
        return switch (region) {
            case "NSW" -> "Australia/Sydney";
            case "VIC" -> "Australia/Melbourne";
            case "QLD" -> "Australia/Brisbane";
            default -> null;
        };
    }

    /**
     * Whether the owner carries the bulk-signup warning (more than 80 owners already created
     * on its registration day at creation time). A {@code null} stored value is treated as
     * {@code false}, so the field is always present in the response.
     */
    default Boolean bulkSignupWarning(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getBulkSignupWarning());
    }

    /**
     * Whether the owner carries the capacity warning (its city already held between 40 and 49
     * owners, approaching the capacity limit of 50, at creation time). A {@code null} stored value
     * is treated as {@code false}, so the field is always present in the response.
     */
    default Boolean capacityWarning(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getCapacityWarning());
    }

    /**
     * Whether the owner was flagged as a possible (soft) duplicate at creation time — not a hard
     * duplicate, but sharing an existing owner's last-name Soundex code and postcode while differing
     * on the identity key (typically a different telephone). A {@code null} stored value is treated
     * as {@code false}, so the field is always present in the response.
     */
    default Boolean possibleDuplicate(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getPossibleDuplicate());
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email address is
     * present, otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        String email = owner.getEmail();
        return (email != null && !email.isEmpty())
            ? OwnerDto.ContactPreferenceEnum.EMAIL
            : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * The owner-identity contract version surfaced as the response's {@code apiVersion}; always
     * {@link IdentityVersion#API_VERSION 2}.
     */
    default Integer apiVersion() {
        return IdentityVersion.API_VERSION;
    }

    /**
     * Groups the owner's version-2 identifiers under the nested {@code identity} object: the stored
     * {@code memberId} and {@code householdId}, and the derived {@code identityKey} (the SHA-256 hex
     * over {@code versionTag + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)};
     * see {@link IdentityKeyNormalizer}). Each identifier is rederived with the version-2 algorithm,
     * so no value produced under version 1 is produced again.
     */
    default OwnerIdentityDto identity(Owner owner) {
        if (owner == null) {
            return null;
        }
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(IdentityKeyNormalizer.derive(owner));
        return identity;
    }

    /**
     * Derives the owner's {@code ageBand} from {@code birthDate}, measured as of the owner's
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} when 18 to 64, and
     * {@code SENIOR} when 65 or older. Returns {@code null} when no birth date is known (or when
     * no reference registration date is available), so the field is absent from the response.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate();
        if (reference == null) {
            reference = LocalDate.now();
        }
        int age = Period.between(owner.getBirthDate(), reference).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's {@code selfLink}, the canonical API path for the owner, formed as
     * {@code '/api/owners/'} followed by the owner's id. Returns {@code null} until the owner
     * has an id, so the field is absent from the response.
     */
    default String selfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * Derives the owner's {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'}. TIER is
     * {@code PREMIUM} when {@link #membershipLevel(Owner)} is 3 or more, otherwise {@code STANDARD}.
     * AREA is {@code METRO} when {@link #locality(Owner)} is a known region (NSW, VIC or QLD),
     * otherwise {@code REGIONAL}.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        Integer level = membershipLevel(owner);
        String tier = (level != null && level >= 3) ? "PREMIUM" : "STANDARD";
        String region = locality(owner);
        boolean metro = "NSW".equals(region) || "VIC".equals(region) || "QLD".equals(region);
        String area = metro ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /**
     * Whether the owner should be flagged for manual review. True when any one of these holds: the
     * owner is a possible (soft) duplicate ({@link #possibleDuplicate(Owner)}), the owner's e-mail
     * domain is disposable-adjacent ({@link EmailNormalizer#isDisposableAdjacent(String)}), or the
     * owner's city is over its soft capacity ({@link #capacityWarning(Owner)}). Otherwise false.
     * The result composes those three sub-flags rather than reading a stored value, so it is always
     * non-null (and thus always present in the response) for a non-null owner.
     */
    default Boolean riskFlag(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(possibleDuplicate(owner))
            || EmailNormalizer.isDisposableAdjacent(owner.getEmail())
            || Boolean.TRUE.equals(capacityWarning(owner));
    }

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
