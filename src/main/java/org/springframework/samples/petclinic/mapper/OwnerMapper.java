package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.controller.CityRegionResolver;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.ClinicService;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /** Used to count the members of an owner's household when deriving derived fields. */
    @Autowired
    protected ClinicService clinicService;

    /** Single source of truth for the city-to-region mapping behind an owner's locality. */
    @Autowired
    protected CityRegionResolver cityRegionResolver;

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's check digit: a single Luhn check digit (0-9) computed over the digits
     * contained in the customerCode, or null when no customerCode has been assigned.
     */
    Integer checkDigit(Owner owner) {
        String customerCode = owner.getCustomerCode();
        return customerCode == null ? null : luhn(customerCode);
    }

    /**
     * The Luhn check digit (0-9) over the digits contained in {@code s}: from the rightmost
     * digit leftward, every second digit is doubled (the rightmost first) with digits over 9
     * reduced by 9, and the check digit makes the total a multiple of ten.
     */
    static int luhn(String s) {
        int sum = 0;
        boolean doubling = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubling) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubling = !doubling;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * The owner's derived identity key: the normalized telephone, the email (or an empty
     * string when absent) and the householdId joined by '|'. This single key is the sole
     * basis for duplicate detection on create — two owners are duplicates only when their
     * whole identityKey matches, so members of one household with different telephones
     * (and hence different keys) are all permitted.
     */
    public static String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * The owner's preferred contact channel: 'EMAIL' when an email address is
     * present, otherwise 'PHONE'.
     */
    String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The canonical region derived from the owner via {@link CityRegionResolver}, preferring the
     * postcode over the city, or 'UNKNOWN' when neither yields a known region.
     */
    String locality(Owner owner) {
        return cityRegionResolver.regionFor(owner.getCity(), owner.getPostcode());
    }

    /**
     * The owner's numeric membership level from 1 to 3: starting at 1, plus 1 when an
     * email address is present, plus 1 when namesakeCount is 0, capped at 3 (level 4 is
     * reserved for tenure).
     */
    public int membershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean uniqueName = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (uniqueName) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * The upper-cased first letters of firstName and lastName, dot-separated with a
     * trailing dot, e.g. 'J.S.'.
     */
    String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

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
