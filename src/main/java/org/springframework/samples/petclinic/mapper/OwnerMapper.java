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

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink",
        expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.util.TelephoneNormalizer.formatForDisplay(owner.getTelephone()))")
    @Mapping(target = "membershipPoints",
        expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(locality(owner))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.util.LocalityResolver.timezone(locality(owner)))")
    @Mapping(target = "apiVersion",
        expression = "java(API_VERSION)")
    @Mapping(target = "identity",
        expression = "java(identity(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(ownerSegment(owner))")
    @Mapping(target = "riskFlag",
        expression = "java(riskFlag(owner))")
    @Mapping(target = "contactPreference",
        expression = "java((owner.getEmail() != null && !owner.getEmail().isBlank()) "
            + "? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE)")
    @Mapping(target = "fiscalYear",
        expression = "java(fiscalYear(owner))")
    @Mapping(target = "ageBand",
        expression = "java(owner.getBirthDate() == null || owner.getRegistrationDate() == null ? null "
            + ": (java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears() < 18 "
            + "? OwnerDto.AgeBandEnum.MINOR "
            + ": java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears() < 65 "
            + "? OwnerDto.AgeBandEnum.ADULT : OwnerDto.AgeBandEnum.SENIOR))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /** The version of the owner identity contract every owner response is produced under. */
    int API_VERSION = 2;

    /**
     * Builds the owner's version-2 identity: the derived {@code memberId}, {@code householdId} and
     * {@code identityKey}, grouped under one object in the response instead of sitting at the top
     * level. The region code mixed into each identifier carries the fixed {@code "V2"} version tag,
     * so every value differs from its version-1 form, while the user-facing {@link #locality}
     * keeps the plain region code.
     */
    default OwnerIdentityDto identity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(owner.getIdentityKey());
        return identity;
    }

    /**
     * Returns the owner's canonical region (locality): the plain region derived from the city and
     * postcode (preferring the postcode), or {@code "UNKNOWN"}. This is not an identifier, so it
     * stays the plain region code (e.g. {@code "NSW"}) and never carries the {@code "V2"} version tag
     * mixed into the identifiers under {@link #identity}.
     */
    default String locality(Owner owner) {
        return org.springframework.samples.petclinic.util.LocalityResolver.resolve(owner.getCity(), owner.getPostcode());
    }

    /**
     * Returns the owner's fiscal year formatted {@code "FY<YY>"}: the FY segment carried in the
     * member id when present, so the value is read from the id itself; otherwise (no member id) the
     * fiscal year of the stored registration date, or {@code null} when neither is present.
     */
    default String fiscalYear(Owner owner) {
        if (owner.getMemberId() != null) {
            return org.springframework.samples.petclinic.util.MemberId.fiscalYear(owner.getMemberId());
        }
        return owner.getRegistrationDate() == null ? null
            : org.springframework.samples.petclinic.util.FiscalYear.label(owner.getRegistrationDate());
    }

    /**
     * Returns the owner's marketing segment formatted {@code "<TIER>_<AREA>"}. TIER is
     * {@code "PREMIUM"} when the membership level is 3 or more, otherwise {@code "STANDARD"}. AREA is
     * {@code "METRO"} when the locality is a known region (NSW, VIC or QLD), otherwise
     * {@code "REGIONAL"}.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String locality = locality(owner);
        String area = ("NSW".equals(locality) || "VIC".equals(locality) || "QLD".equals(locality))
            ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.valueOf(tier + "_" + area);
    }

    /**
     * Computes the owner's membership points: 0 to start, plus 2 when an email address is present,
     * plus 1 when the namesake count is 0, plus 2 for a household of 3 or more, and plus 3 for
     * tenure of at least one elapsed fiscal year (the fiscal year of today is later than the fiscal
     * year of the registration date).
     */
    default int membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (owner.getRegistrationDate() != null
            && org.springframework.samples.petclinic.util.FiscalYear.elapsed(
                owner.getRegistrationDate(), java.time.LocalDate.now()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Returns the owner's membership level. When a level was assigned and stored at creation time
     * (the already-capped level: a new owner's level cannot exceed one above the current maximum
     * membership level among their existing household members) it is returned as-is; otherwise the
     * level is derived from the owner's membership points. Deriving the level here, per owner, keeps
     * any owner-scoped adjustment to the level (the household cap, or the fallback points mapping) in
     * a single home, leaving {@link #membershipLevel(int)} as the plain points-to-level table.
     */
    default int membershipLevel(Owner owner) {
        return owner.getMembershipLevel() != null
            ? owner.getMembershipLevel()
            : membershipLevel(membershipPoints(owner));
    }

    /**
     * Maps membership points to a membership level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4
     * for 6 or more.
     */
    default int membershipLevel(int membershipPoints) {
        if (membershipPoints <= 1) {
            return 1;
        }
        if (membershipPoints <= 3) {
            return 2;
        }
        if (membershipPoints <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * The registrable labels of the known disposable-email providers (the label immediately before
     * the final TLD of each blocklisted domain). An owner's email domain is "disposable-adjacent"
     * when it shares one of these labels, which flags a subdomain of a disposable provider or the
     * same provider name under a different TLD.
     */
    java.util.Set<String> DISPOSABLE_EMAIL_LABELS =
        java.util.Set.of("mailinator", "tempmail", "guerrillamail");

    /**
     * Whether this owner is flagged for manual risk review, derived on read. True when any of these
     * hold: the owner is a possible duplicate ({@code possibleDuplicate}), the owner's email domain
     * is disposable-adjacent (see {@link #disposableAdjacentEmail(String)}), or the owner's city was
     * over its soft capacity when the owner was created ({@code capacityWarning}); false otherwise.
     */
    default boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || Boolean.TRUE.equals(owner.getCapacityWarning())
            || disposableAdjacentEmail(owner.getEmail());
    }

    /**
     * Whether the (already lower-cased) email's domain is "disposable-adjacent": it is not itself on
     * the disposable blocklist (such a value is rejected at create) but belongs to the same family as
     * a known disposable-email provider, sharing the provider's registrable label (the label
     * immediately before the final TLD). A different top-level domain (e.g. {@code mailinator.net})
     * or a subdomain (e.g. {@code inbox.mailinator.com}) of a disposable provider therefore counts. A
     * {@code null} or domain-less email is never adjacent.
     */
    default boolean disposableAdjacentEmail(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        int lastDot = domain.lastIndexOf('.');
        if (lastDot <= 0) {
            return false;
        }
        int prevDot = domain.lastIndexOf('.', lastDot - 1);
        String label = domain.substring(prevDot + 1, lastDot);
        return DISPOSABLE_EMAIL_LABELS.contains(label);
    }

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
