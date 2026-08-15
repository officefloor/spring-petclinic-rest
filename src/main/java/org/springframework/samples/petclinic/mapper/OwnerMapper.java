package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /**
     * The highest membership level derivable at creation. Level 4 is reserved for tenure and is never
     * produced here, so the computed level is capped at this value.
     */
    private static final int MAX_MEMBERSHIP_LEVEL = 3;

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.mapper.OwnerLocality.of(owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Computes an owner's numeric membership level, assigned at creation. The level starts at 1, gains 1
     * when an email is present, gains a further 1 when {@code namesakeCount} is 0, and is capped at
     * {@value #MAX_MEMBERSHIP_LEVEL} (level 4 is reserved for tenure).
     *
     * @param owner the owner whose level is being computed
     * @return the membership level (between 1 and {@value #MAX_MEMBERSHIP_LEVEL} inclusive)
     */
    protected Integer membershipLevel(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX_MEMBERSHIP_LEVEL);
    }

    /**
     * Computes the owner's {@code checkDigit}: a single Luhn check digit (0-9) over the digits contained in
     * the owner's {@code customerCode}. Non-digit characters (such as the letters and dashes in the code) are
     * skipped; the rightmost digit is doubled and every second digit thereafter, digits exceeding 9 after
     * doubling have 9 subtracted, and the check digit is {@code (10 - sum % 10) % 10}.
     *
     * @param owner the owner whose check digit is being computed
     * @return the Luhn check digit (between 0 and 9 inclusive) over the customer code's digits
     */
    protected Integer checkDigit(Owner owner) {
        String code = owner.getCustomerCode() == null ? "" : owner.getCustomerCode();
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

    /**
     * Derives an owner's preferred contact channel at read time: {@code 'EMAIL'} when an email address is
     * present, otherwise {@code 'PHONE'}.
     *
     * @param owner the owner whose contact preference is being computed
     * @return {@code 'EMAIL'} when the owner has a non-blank email, otherwise {@code 'PHONE'}
     */
    protected String contactPreference(Owner owner) {
        return (owner.getEmail() != null && !owner.getEmail().isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * Derives an owner's age band from {@code birthDate}, measured against {@code registrationDate}: the
     * completed years between the two dates place the owner in {@code 'MINOR'} (under 18), {@code 'ADULT'}
     * (18 to 64 inclusive) or {@code 'SENIOR'} (65 or over). Returns {@code null} when the owner has no
     * {@code birthDate}, so the field is simply absent for owners created without one.
     *
     * @param owner the owner whose age band is being derived
     * @return {@code 'MINOR'}, {@code 'ADULT'} or {@code 'SENIOR'}, or {@code null} when no birth date is set
     */
    protected String ageBand(Owner owner) {
        if (owner.getBirthDate() == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int years = Period.between(owner.getBirthDate(), reference).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * Derives an owner's {@code identityKey}: the normalized telephone, the email (or an empty string when
     * absent) and the household id (or an empty string when absent), joined by {@code '|'} in that order
     * (e.g. {@code '+61412345678||a1b2c3d4e5f6a7b8'}). This is the single key used for duplicate detection,
     * so the value returned here matches the one the create endpoint compares.
     *
     * @param owner the owner whose identity key is being derived
     * @return the owner's identity key
     */
    protected String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    public OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
