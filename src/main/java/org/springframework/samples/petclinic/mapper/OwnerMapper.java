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

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(toMembershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(toCheckDigit(owner))")
    @Mapping(target = "membershipLevel", expression = "java(toMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    @Mapping(target = "contactPreference", expression = "java(toContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(toIdentityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(toAgeBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(toTelephoneDisplay(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's {@code telephoneDisplay}: the stored E.164 {@code telephone} formatted
     * for humans (see {@link org.springframework.samples.petclinic.rest.function.owner.E164Telephone#display}).
     * The raw {@code telephone} is left in E.164 form.
     */
    default String toTelephoneDisplay(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.E164Telephone
                .display(owner.getTelephone());
    }

    /**
     * Derives the owner's {@code ageBand} from its {@code birthDate} measured against its
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to 64,
     * {@code SENIOR} at 65 or older. Returns null when either date is absent (so no band is
     * reported for owners with no supplied birth date).
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum toAgeBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.ADULT;
        }
        return org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's {@code identityKey}: the single value that consolidates all
     * duplicate detection, {@code normalizedTelephone + '|' + (email or empty) + '|' +
     * (householdId or empty)}.
     */
    default String toIdentityKey(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(owner);
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email is present,
     * otherwise {@code PHONE}.
     */
    default String toContactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Derives the owner's locality (region) from its region-and-hash identity: the
     * {@code customerCode} is {@code <REGION>-<HASH8>}, so the locality is the REGION segment
     * preceding the hash. Falls back to deriving the region directly from the postcode/city
     * (see {@link org.springframework.samples.petclinic.rest.function.owner.OwnerCustomerCode})
     * only for unmigrated owners that carry no customer code.
     */
    default String toLocality(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            if (dash > 0) {
                return customerCode.substring(0, dash);
            }
        }
        return org.springframework.samples.petclinic.rest.function.owner.OwnerCustomerCode
                .region(owner.getPostcode(), owner.getCity());
    }

    /**
     * Derives the owner's numeric membership level (1 to 3), computed on creation: start at 1;
     * add 1 when an email is present; add 1 when the owner has no namesakes ({@code namesakeCount}
     * is 0); capped at 3 (level 4 is reserved for tenure).
     */
    default Integer toMembershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean unique = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (unique) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Derives the owner's membership number as {@code <customerCode>-M<YY>}, where YY is the
     * last two digits of the registration-date year (e.g. {@code NSW-1A2B3C4D-M26}). Returns
     * null when either the customer code or registration date is absent (e.g. unmigrated seed
     * data).
     */
    default String toMembershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M" + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Derives the owner's {@code checkDigit}: the Luhn check digit (0-9) computed over the
     * digits contained in the {@code customerCode}. Returns null when the customer code is
     * absent (e.g. unmigrated seed data).
     */
    default Integer toCheckDigit(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
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

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdSize", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
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
