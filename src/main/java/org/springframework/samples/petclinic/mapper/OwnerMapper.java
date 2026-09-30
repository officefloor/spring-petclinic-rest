package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", source = "owner", qualifiedByName = "formatDisplayName")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "formatInitials")
    @Mapping(target = "telephoneDisplay", source = "owner", qualifiedByName = "formatTelephoneDisplay")
    @Mapping(target = "fiscalYear", source = "owner", qualifiedByName = "formatFiscalYear")
    @Mapping(target = "membershipPoints", source = "owner", qualifiedByName = "formatMembershipPoints")
    @Mapping(target = "membershipLevel", source = "owner", qualifiedByName = "formatMembershipLevel")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "formatLocality")
    @Mapping(target = "ownerSegment", source = "owner", qualifiedByName = "formatOwnerSegment")
    @Mapping(target = "timezone", source = "owner", qualifiedByName = "formatTimezone")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "formatContactPreference")
    @Mapping(target = "selfLink", source = "owner", qualifiedByName = "formatSelfLink")
    @Mapping(target = "apiVersion", constant = "2")
    @Mapping(target = "identity", source = "owner", qualifiedByName = "formatIdentity")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "riskFlag", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Groups the owner's version-2 identity values (memberId, householdId and identityKey) under the
     * nested {@code identity} object of the response. Each value is derived with the fixed {@code 'V2'}
     * version tag mixed into the region code used inside the identifier (see
     * {@link Owner#deriveIdentityRegion()}), so none matches its version-1 form. Returns {@code null}
     * when the owner is absent.
     */
    @Named("formatIdentity")
    default OwnerIdentityDto formatIdentity(Owner owner) {
        if (owner == null) {
            return null;
        }
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(owner.getIdentityKey());
        return identity;
    }

    /**
     * Formats an owner's canonical resource URL as {@code '/api/owners/'} followed by its id, for the
     * read-only {@code selfLink} field. Null when the owner or its id is absent.
     */
    @Named("formatSelfLink")
    default String formatSelfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * Derives an owner's preferred contact channel for the read-only {@code contactPreference}
     * field: {@code 'EMAIL'} when an email address is present, otherwise {@code 'PHONE'}.
     */
    @Named("formatContactPreference")
    default String formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Derives an owner's user-facing {@code locality}: the plain region code derived from the owner's
     * postcode/city (see {@link Owner#deriveRegion()}), which prefers the postcode over the city and
     * defaults to {@code 'UNKNOWN'}. This is the untagged region: the {@code 'V2'} version tag mixed
     * into the identifiers never appears here, so the locality stays a plain region such as
     * {@code 'NSW'}. Returns {@code 'UNKNOWN'} when the owner is absent.
     */
    @Named("formatLocality")
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return "UNKNOWN";
        }
        return owner.deriveRegion();
    }

    /**
     * Derives an owner's {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'}. TIER is
     * {@code 'PREMIUM'} when the owner's {@code membershipLevel} is 3 or more, otherwise
     * {@code 'STANDARD'}. AREA is {@code 'METRO'} when the owner's locality is a known region
     * (NSW, VIC or QLD), otherwise {@code 'REGIONAL'}. Both the membership level and the locality
     * are taken from {@link #formatMembershipLevel(Owner)} and {@link #formatLocality(Owner)}, so
     * the segment always agrees with those reported fields.
     */
    @Named("formatOwnerSegment")
    default String formatOwnerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        String tier = formatMembershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(formatLocality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Fixed region-to-timezone table mapping each canonical region to its IANA timezone name.
     */
    Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Derives an owner's {@code timezone} as the IANA timezone name for its locality/region, using
     * the fixed region-to-timezone table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane). The region is taken from {@link #formatLocality(Owner)}, so the
     * timezone always agrees with the reported locality. Returns {@code null} when the region is not
     * in the table (e.g. {@code 'UNKNOWN'}).
     */
    @Named("formatTimezone")
    default String formatTimezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return REGION_TIMEZONE.get(formatLocality(owner));
    }

    /**
     * Computes an owner's membership points for the read-only {@code membershipPoints} field, using
     * the household size resolved onto the owner at read time. Delegates to
     * {@link Owner#membershipPoints(Integer)} for the point rules.
     */
    @Named("formatMembershipPoints")
    default Integer formatMembershipPoints(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.membershipPoints(owner.getHouseholdSize());
    }

    /**
     * Derives an owner's numeric membership level for the read-only {@code membershipLevel} field,
     * using the household size resolved onto the owner at read time. Delegates to
     * {@link Owner#membershipLevel(Integer)}, which maps points to a level and applies the owner's
     * membership-level cap.
     */
    @Named("formatMembershipLevel")
    default Integer formatMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.membershipLevel(owner.getHouseholdSize());
    }

    /**
     * Formats an owner's fiscal year as {@code 'FY<YY>'}, where YY is the last two digits of the
     * fiscal year derived from the business-day-adjusted registration date (e.g. {@code 'FY27'}),
     * for the read-only {@code fiscalYear} field. Each fiscal year starts on 1 July and is named by
     * the calendar year in which it ends. Null when the registration date is absent.
     */
    @Named("formatFiscalYear")
    default String formatFiscalYear(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("FY%02d", Owner.fiscalYear(owner.getRegistrationDate()) % 100);
    }

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'} for the read-only
     * {@code displayName} field.
     */
    @Named("formatDisplayName")
    default String formatDisplayName(Owner owner) {
        return owner == null ? null : owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot (e.g. {@code 'J.S.'}), for the
     * read-only {@code initials} field.
     */
    @Named("formatInitials")
    default String formatInitials(Owner owner) {
        return owner == null ? null
            : Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * Formats the owner's stored E.164 telephone for human display as the country code, a space
     * and the national digits grouped in threes (e.g. {@code '+61412345678'} becomes
     * {@code '+61 412 345 678'}), for the read-only {@code telephoneDisplay} field. The raw
     * {@code telephone} field stays in E.164 form. Null when no telephone is present.
     */
    @Named("formatTelephoneDisplay")
    default String formatTelephoneDisplay(Owner owner) {
        return owner == null ? null : TelephoneNormalizer.toDisplay(owner.getTelephone());
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "addressLine1", source = "addressLine1", qualifiedByName = "normalizeAddress")
    @Mapping(target = "addressLine2", source = "addressLine2", qualifiedByName = "normalizeAddress")
    @Mapping(target = "address", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Resolves the owner's stored {@code address} after the field mapping runs, from the already
     * normalized {@code addressLine1}/{@code addressLine2} fields set on the target (preferred when
     * present) with the normalized flat {@code address} as the fallback. See
     * {@link AddressNormalizer#compose(String, String, String)} for the composition rule.
     */
    @AfterMapping
    default void composeAddress(OwnerFieldsDto ownerDto, @MappingTarget Owner owner) {
        owner.setAddress(AddressNormalizer.compose(
            owner.getAddressLine1(), owner.getAddressLine2(), normalizeAddress(ownerDto.getAddress())));
    }

    /**
     * Normalizes a postal address for persistence: whitespace is trimmed and collapsed, the
     * text is upper-cased and common street-type abbreviations are expanded (e.g.
     * {@code '  12  main  st '} becomes {@code '12 MAIN STREET'}). The required-field rule is
     * enforced separately; an address blank after normalization is rejected.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Normalizes a telephone number for persistence into canonical E.164 form (e.g.
     * {@code '0412 345 678'} becomes {@code '+61412345678'}). The formability rule is
     * enforced separately by {@code TelephoneValidation}.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return TelephoneNormalizer.toE164(telephone);
    }

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
