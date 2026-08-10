package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "householdId", expression = "java(householdId(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives an owner's locality from its city using a fixed city-to-region table
     * ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}), returning the
     * canonical region string or {@code "UNKNOWN"} when the city is not in the table.
     *
     * @param owner the owner to derive the locality for
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    default String locality(Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return "UNKNOWN";
        }
        return switch (city) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * Derives an owner's numeric membership level, fixed at creation. Starts at {@code 1}, plus
     * {@code 1} when an email is present (non-blank), plus {@code 1} when the owner's namesake count
     * is {@code 0}, capped at {@code 3} (level {@code 4} is reserved for tenure).
     *
     * @param owner the owner to derive the membership level for
     * @return the membership level, between {@code 1} and {@code 3}
     */
    default Integer membershipLevel(Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Derives an owner's membership number, formatted {@code <customerCode>-M<YY>} where
     * {@code <customerCode>} is the owner's customer code and {@code YY} is the last two digits
     * (zero-padded) of the owner's registration date year (e.g. {@code LON-SMI-0007-M26}).
     *
     * @param owner the owner to derive the membership number for
     * @return the formatted membership number
     */
    default String membershipNumber(Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        return owner.getCustomerCode() + "-M" + yy;
    }

    /**
     * Derives an owner's household identifier: a stable value shared by every owner with the same
     * last name and address (compared case-insensitively with collapsed whitespace). Because it is
     * derived deterministically from those fields, owners created via the {@code sharesHousehold}
     * flag - which by definition have a matching last name and address - receive the same value.
     *
     * @param owner the owner to derive the household identifier for
     * @return a {@code HH-} prefixed identifier, never blank
     */
    default String householdId(Owner owner) {
        String key = normalizeHouseholdField(owner.getLastName()) + ' '
            + normalizeHouseholdField(owner.getAddress());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("HH-");
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Collapses surrounding and internal whitespace and lower-cases a value so household fields can
     * be compared case-insensitively with collapsed whitespace. A {@code null} value normalizes to
     * the empty string. Mirrors the normalization used when enforcing the household rule on create.
     *
     * <p>Declared {@code private} so MapStruct does not treat it as an implicit String-to-String
     * conversion and apply it to unrelated String properties (last name, address, ...).
     */
    private String normalizeHouseholdField(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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
