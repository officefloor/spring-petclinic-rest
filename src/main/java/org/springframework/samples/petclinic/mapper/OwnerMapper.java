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

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "checkDigit", expression = "java(org.springframework.samples.petclinic.util.LuhnCheckDigit.of(owner.getCustomerCode()))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", org.springframework.samples.petclinic.util.FiscalYear.yearOf(owner.getRegistrationDate()) % 100))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "contactPreference", expression = "java((owner.getEmail() != null && !owner.getEmail().isBlank()) ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE)")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.util.OwnerIdentity.identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.util.TelephoneNormalizer.toDisplay(owner.getTelephone()))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's locality (region), the single value every region-derived field is built from: the
     * {@code locality} itself, the {@link #timezone timezone} and the {@link #ownerSegment
     * ownerSegment} area. It is the region component of the owner's {@code customerCode} (see
     * {@link org.springframework.samples.petclinic.util.CustomerCode#region}). Routing all three
     * readers through this one accessor keeps them agreed on how an owner's region is obtained from
     * its identity.
     */
    default String locality(Owner owner) {
        return org.springframework.samples.petclinic.util.CustomerCode.region(owner.getCustomerCode());
    }

    /**
     * The owner's {@code fiscalYear} label, {@code FY<YY>} for the fiscal year its
     * business-day-adjusted {@code registrationDate} falls in (see
     * {@link org.springframework.samples.petclinic.util.FiscalYear#label}).
     */
    default String fiscalYear(Owner owner) {
        return org.springframework.samples.petclinic.util.FiscalYear.label(owner.getRegistrationDate());
    }

    /**
     * Derives the owner's IANA {@code timezone} from its locality (region) via a fixed
     * region-to-timezone table: {@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne}
     * and {@code QLD -> Australia/Brisbane}. Returns {@code null} for a region not in the table.
     */
    default String timezone(Owner owner) {
        String region = locality(owner);
        if (region == null) {
            return null;
        }
        return switch (region) {
            case "NSW" -> "Australia/Sydney";
            case "VIC" -> "Australia/Melbourne";
            case "QLD" -> "Australia/Brisbane";
            default -> null;
        };
    }

    /**
     * Derives the owner's {@code ageBand} from its {@code birthDate}, measured against its
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to 64 and
     * {@code SENIOR} at 65 or older. Returns {@code null} when no birth date is present.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate asOf = owner.getRegistrationDate();
        if (birthDate == null || asOf == null) {
            return null;
        }
        int years = java.time.Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (years < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Computes the owner's {@code membershipPoints}. Starts at 0; adds 2 when a non-blank email is
     * present; adds 1 when {@code namesakeCount} is 0; adds 2 for a household of 3 or more; adds 3
     * when at least one fiscal year has elapsed since {@code registrationDate} (measured from
     * {@code registrationDate} to today, counting 1-July boundaries crossed).
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
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null
                && org.springframework.samples.petclinic.util.FiscalYear.elapsed(
                        registrationDate, java.time.LocalDate.now()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Reports the owner's {@code membershipLevel}. Uses the level stored on the owner when present
     * (as stamped on creation), otherwise falls back to the level {@linkplain #derivedMembershipLevel
     * derived} from its {@code membershipPoints}.
     */
    default int membershipLevel(Owner owner) {
        Integer stored = owner.getMembershipLevel();
        return stored != null ? stored : derivedMembershipLevel(owner);
    }

    /**
     * Derives the owner's {@code membershipLevel} from {@code membershipPoints}: 1 for 0-1 points,
     * 2 for 2-3 points, 3 for 4-5 points and 4 for 6 or more points.
     */
    default int derivedMembershipLevel(Owner owner) {
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
     * Derives the owner's {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'}. TIER is
     * {@code PREMIUM} when {@link #membershipLevel(Owner) membershipLevel} is 3 or more, otherwise
     * {@code STANDARD}. AREA is {@code METRO} when the owner's locality is a known region
     * ({@code NSW}, {@code VIC} or {@code QLD}), otherwise {@code REGIONAL}.
     */
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String region = locality(owner);
        boolean metro = "NSW".equals(region) || "VIC".equals(region) || "QLD".equals(region);
        String area = metro ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.fromValue(tier + "_" + area);
    }

    /**
     * Composes the owner's {@code salutation} from its honorific {@code title} and {@code lastName}:
     * {@code title + ' ' + lastName} when a non-blank title is present, or just the {@code lastName}
     * when no title is given.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
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
