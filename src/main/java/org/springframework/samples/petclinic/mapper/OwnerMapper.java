package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" "
            + "+ owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "membershipLevel",
        expression = "java(toMembershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(toLocality(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(toContactPreference(owner))")
    @Mapping(target = "identityKey",
        expression = "java(toIdentityKey(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derive an owner's duplicate-detection identity key: the normalized E.164 telephone, the
     * lower-cased email (empty when none), and the household id (empty when none), joined with
     * {@code '|'}. Two owners are duplicates only when their whole identity keys are equal.
     *
     * <p>This is the single source of truth for the key: it is both returned on the DTO and used by
     * the create endpoint's 409 duplicate check, so the value clients see is exactly the value that
     * governs rejection. The "email present" rule reuses {@link #hasEmail(Owner)}.
     */
    default String toIdentityKey(Owner owner) {
        String email = hasEmail(owner) ? owner.getEmail() : "";
        String household = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return owner.getTelephone() + "|" + email + "|" + household;
    }

    /**
     * Whether the owner has an email address present (non-null and non-empty). Single source of truth
     * for the "is an email present" rule shared by the derived fields below.
     */
    default boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isEmpty();
    }

    /**
     * Derive an owner's preferred contact channel: 'EMAIL' when an email address is present,
     * otherwise 'PHONE'.
     */
    default String toContactPreference(Owner owner) {
        return hasEmail(owner) ? "EMAIL" : "PHONE";
    }

    /**
     * Derive an owner's numeric membership level (1 to 3): start at 1; add 1 when an email address is
     * present; add 1 when the owner has no namesakes (namesakeCount is 0); capped at 3 (level 4 is
     * reserved for tenure).
     */
    default Integer toMembershipLevel(Owner owner) {
        int level = 1;
        if (hasEmail(owner)) {
            level++;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Fixed city-to-region table used as the fall-back when deriving an owner's locality.
     */
    Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Fixed region-to-inclusive-postcode-range table used to derive an owner's locality from the
     * postcode: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. Each entry is {@code {low, high}}
     * inclusive.
     */
    Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derive an owner's locality (region), preferring the postcode: when the postcode is present and
     * falls within a known region's range (see {@link #REGION_POSTCODE_RANGES}) that region wins. When
     * the postcode is absent or in no known range, fall back to the fixed {@link #CITY_REGION} table.
     * Returns 'UNKNOWN' when neither the postcode nor the city resolves a region. Preferring the
     * postcode returns the same region for known cities but disambiguates cities that share a name.
     */
    default String toLocality(Owner owner) {
        // Prefer the postcode: the region whose range contains it wins. The logic is inlined (rather
        // than a String -> String helper) because MapStruct would treat such a helper as an implicit
        // type conversion and wrongly apply it to other String properties (e.g. telephone).
        String postcode = owner.getPostcode();
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            }
            catch (NumberFormatException ignored) {
                // Non-numeric postcode: fall back to the city table below.
            }
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
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
