package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.samples.petclinic.util.RegionTimezones;
import org.springframework.samples.petclinic.util.TelephoneNormalizer;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(toSelfLink(owner))")
    @Mapping(target = "displayName", expression = "java(toDisplayName(owner))")
    @Mapping(target = "salutation", expression = "java(owner.getSalutation())")
    @Mapping(target = "initials", expression = "java(toInitials(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    @Mapping(target = "timezone", expression = "java(toTimezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(toContactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(toAgeBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(toOwnerSegment(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(toTelephoneDisplay(owner))")
    @Mapping(target = "fiscalYear", expression = "java(owner.getFiscalYear())")
    @Mapping(target = "riskFlag", expression = "java(owner.isRiskFlag())")
    @Mapping(target = "apiVersion", constant = "2")
    @Mapping(target = "identity", expression = "java(toIdentity(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Group the owner's version-2 identifiers under the nested {@code identity} object of the owner
     * response: the unified {@code memberId}, the duplicate-detection {@code identityKey} and the
     * shared {@code householdId}, each already derived with the version-2 algorithm that mixes in the
     * fixed {@code 'V2'} version tag.
     */
    default OwnerIdentityDto toIdentity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setIdentityKey(owner.getIdentityKey());
        identity.setHouseholdId(owner.getHouseholdId());
        return identity;
    }

    /**
     * Derive the owner's read-only {@code selfLink}: the canonical API path of this owner,
     * {@code '/api/owners/<id>'}, built from the owner's assigned id. Returns {@code null}
     * when the owner has no id yet, so the field is omitted from the response.
     */
    default String toSelfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * Derive the owner's read-only {@code telephoneDisplay}: the stored E.164 {@code telephone}
     * formatted for humans (country code, a space, then the national digits grouped in threes),
     * e.g. {@code '+61412345678'} becomes {@code '+61 412 345 678'}. Returns {@code null} when the
     * owner has no telephone, so the field is omitted from the response.
     */
    default String toTelephoneDisplay(Owner owner) {
        return TelephoneNormalizer.toDisplay(owner.getTelephone());
    }

    /**
     * Derive the owner's read-only {@code ageBand} enum from its {@code birthDate}, computed against
     * the owner's {@code registrationDate} (see {@link Owner#getAgeBand()}). Returns {@code null}
     * when the owner has no {@code birthDate}, so the field is omitted from the response.
     */
    default OwnerDto.AgeBandEnum toAgeBand(Owner owner) {
        String ageBand = owner.getAgeBand();
        return ageBand == null ? null : OwnerDto.AgeBandEnum.fromValue(ageBand);
    }

    /**
     * Format an owner's stored names as {@code 'LastName, FirstName'} for the
     * read-only {@code displayName} field returned on the Owner DTO.
     */
    default String toDisplayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Build the owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot (e.g. {@code 'J.S.'}) for the
     * read-only {@code initials} field returned on the Owner DTO.
     */
    default String toInitials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * Derive the owner's read-only {@code locality}: the plain region code resolved directly from the
     * owner's postcode range first ({@code NSW 2000-2099, VIC 3000-3099, QLD 4000-4099}), then the
     * canonical region of the owner's city ({@code Sydney->NSW, Melbourne->VIC, Brisbane->QLD}), else
     * {@code 'UNKNOWN'} (see {@link Owner#getLocality()}). The locality is not an identifier, so it is
     * always the plain region code and never carries the version-2 {@code 'V2'} tag embedded inside
     * the owner's {@code memberId}.
     */
    default String toLocality(Owner owner) {
        return owner.getLocality();
    }

    /**
     * Derive the owner's read-only {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'} (see
     * {@link Owner#getOwnerSegment()}). TIER is {@code PREMIUM} when the owner's
     * {@code membershipLevel} is 3 or more, otherwise {@code STANDARD}; AREA is {@code METRO} when the
     * owner's plain {@link #toLocality(Owner) locality} is a known region, otherwise
     * {@code REGIONAL}. The region used is the plain locality, so the version-2 {@code 'V2'} tag
     * embedded inside the owner's identifiers never leaks into the owner segment.
     */
    default OwnerDto.OwnerSegmentEnum toOwnerSegment(Owner owner) {
        return OwnerDto.OwnerSegmentEnum.fromValue(owner.getOwnerSegment());
    }

    /**
     * Derive the owner's read-only {@code timezone}: the IANA timezone name for the owner's
     * locality/region, looked up from the fixed region-to-timezone table ({@code NSW->Australia/Sydney,
     * VIC->Australia/Melbourne, QLD->Australia/Brisbane}). The region is the same plain value reported
     * as the owner's {@code locality} (see {@link #toLocality(Owner)}), so timezone and locality stay
     * in lock-step. Returns {@code null} when the region has no mapped timezone, so the field is
     * omitted from the response.
     */
    default String toTimezone(Owner owner) {
        return RegionTimezones.timezoneOf(toLocality(owner));
    }

    /**
     * Derive the owner's read-only {@code contactPreference}: {@code EMAIL} when the owner has an
     * email address, otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum toContactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank()
            ? OwnerDto.ContactPreferenceEnum.EMAIL
            : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    @Mapping(target = "idempotencyKey", ignore = true)
    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    @Mapping(target = "address", expression = "java(composeAddress(ownerDto))")
    @Mapping(target = "addressLine1", source = "addressLine1", qualifiedByName = "normalizeAddress")
    @Mapping(target = "addressLine2", source = "addressLine2", qualifiedByName = "normalizeAddress")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Compose an owner's canonical {@code address} from whichever address form the request supplied.
     * The structured form is preferred: when {@code addressLine1} is non-blank after normalization,
     * the result is the normalized {@code addressLine1}, with a single space and the normalized
     * {@code addressLine2} appended when an {@code addressLine2} is present. Otherwise it falls back
     * to the normalized flat {@code address}. Each part is normalized with {@link AddressNormalizer}
     * (trimmed and whitespace-collapsed, upper-cased, street-type abbreviations expanded), so the
     * stored address is in one canonical shape regardless of the form used. Returns {@code null} when
     * no address was supplied in either form.
     */
    default String composeAddress(OwnerFieldsDto ownerDto) {
        String line1 = AddressNormalizer.normalize(ownerDto.getAddressLine1());
        if (line1 != null) {
            String line2 = AddressNormalizer.normalize(ownerDto.getAddressLine2());
            return line2 == null ? line1 : line1 + " " + line2;
        }
        return AddressNormalizer.normalize(ownerDto.getAddress());
    }

    /**
     * Normalize a postal address to its canonical form (trimmed and whitespace-collapsed,
     * upper-cased, with common street-type abbreviations expanded) so it is stored, returned and
     * compared in one shape. Blank-after-normalization is rejected separately by
     * {@code AddressValidation} on the inbound DTO, so this only performs the transform the stored
     * value needs.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Normalize a telephone number to canonical E.164 form (a leading {@code '+'}, country
     * code and national digits) so it is stored and returned in one comparable form and
     * duplicates can be detected by comparing these values. Format validity is enforced
     * separately by {@code TelephoneValidation} on the inbound DTO, so this only performs
     * the transform the stored value needs.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return TelephoneNormalizer.toE164(telephone);
    }

    /**
     * Normalize an email address by trimming surrounding whitespace and lower-casing
     * it so it is stored and returned in a canonical form. Syntactic validity is
     * enforced separately by {@code @Email} on the inbound DTO, so this only performs
     * the transform the stored value needs.
     */
    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
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
