package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", source = "owner", qualifiedByName = "formatDisplayName")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "formatInitials")
    @Mapping(target = "membershipNumber", source = "owner", qualifiedByName = "formatMembershipNumber")
    @Mapping(target = "membershipLevel", source = "owner", qualifiedByName = "formatMembershipLevel")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "formatLocality")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "formatContactPreference")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /** Fixed city-to-region table used to derive an owner's {@code locality}. */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region to inclusive 4-digit postcode range {low, high}, used to derive an
     *  owner's {@code locality} from its postcode ahead of the city table. */
    java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

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
     * Derives an owner's {@code locality} by preferring the postcode: the region whose
     * inclusive 4-digit range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) contains the
     * postcode is used first. When the postcode is absent or in no known range, it falls
     * back to the fixed city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC,
     * Brisbane-&gt;QLD), returning {@code 'UNKNOWN'} when the city is not in the table.
     */
    @Named("formatLocality")
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("^[0-9]{4}$")) {
            int value = Integer.parseInt(postcode);
            for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Determines an owner's numeric membership level for the read-only {@code membershipLevel}
     * field, assigned at creation and ranging from 1 to 3: it starts at 1, gains 1 when an email
     * address is present, gains 1 when the owner had no namesakes at creation (namesakeCount is 0),
     * and is capped at 3. Level 4 is reserved for tenure.
     */
    @Named("formatMembershipLevel")
    default Integer formatMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Formats an owner's membership number as {@code '<customerCode>-M<YY>'}, where YY is the last
     * two digits of the registrationDate year (e.g. {@code 'SYD-SMI-0007-M26'}), for the read-only
     * {@code membershipNumber} field. Null when the customer code or registration date is absent.
     */
    @Named("formatMembershipNumber")
    default String formatMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
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

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "address", source = "address", qualifiedByName = "normalizeAddress")
    Owner toOwner(OwnerFieldsDto ownerDto);

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
