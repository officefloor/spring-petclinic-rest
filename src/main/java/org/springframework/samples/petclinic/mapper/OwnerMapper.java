package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /**
     * More than this many owners already sharing a {@code registrationDate} raises the
     * {@code bulkSignupWarning} on responses for that day.
     */
    private static final int BULK_SIGNUP_WARNING_THRESHOLD = 80;

    @Autowired
    protected OwnerRepository ownerRepository;

    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
            expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's preferred contact channel: {@code EMAIL} when an email address is
     * present (non-null and non-blank), otherwise {@code PHONE}.
     */
    protected OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Fixed city-to-region table: the owner's canonical region derived from its city.
     */
    static final java.util.Map<String, String> CITY_REGION = java.util.Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Region -> inclusive 4-digit postcode range {low, high}: NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099.
     */
    static final java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /**
     * Derives the owner's {@code locality} (canonical region string), preferring the
     * postcode: the region whose range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099)
     * contains the owner's postcode wins. When the postcode is absent or in no known
     * range, it falls back to the fixed city-to-region table
     * ({@code Sydney->NSW, Melbourne->VIC, Brisbane->QLD}), or {@code "UNKNOWN"} when the
     * city is not in the table either.
     */
    protected String locality(Owner owner) {
        String region = regionFromPostcode(owner.getPostcode());
        if (region != null) {
            return region;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * The region whose inclusive postcode range contains the given 4-digit postcode, or
     * {@code null} when the postcode is absent, non-numeric, or in no known range.
     */
    private static String regionFromPostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The membership level is capped at this value; level 4 is reserved for tenure.
     */
    private static final int MAX_MEMBERSHIP_LEVEL = 3;

    /**
     * Returns the owner's membership level, a number from 1 to
     * {@value #MAX_MEMBERSHIP_LEVEL} assigned at creation: it starts at 1, gains 1 when
     * an email address is present, gains 1 when the owner has no namesakes
     * (namesakeCount is 0), and is capped at {@value #MAX_MEMBERSHIP_LEVEL} (level 4 is
     * reserved for tenure).
     */
    public static int membershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            level++;
        }
        return Math.min(level, MAX_MEMBERSHIP_LEVEL);
    }

    /**
     * Builds the membership number '<customerCode>-M<YY>', where YY is the last two digits of
     * the registrationDate year (e.g. 'SMI-0007-M26'). Returns {@code null} when either the
     * customer code or the registration date is absent (e.g. legacy seed owners).
     */
    protected String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * True when more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} other owners have already
     * been created for this owner's {@code registrationDate}, using the same per-day
     * accumulation the daily create limit enforces (the owner itself is excluded). Returns
     * {@code false} when the owner has no registration date (e.g. legacy seed owners).
     */
    protected boolean bulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long count = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
                .count();
        return count > BULK_SIGNUP_WARNING_THRESHOLD;
    }

    /**
     * The Luhn check digit (0-9) computed over the digits contained in the owner's
     * {@code customerCode}. Returns {@code null} when the customer code is absent (e.g.
     * legacy seed owners).
     */
    protected Integer checkDigit(Owner owner) {
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
