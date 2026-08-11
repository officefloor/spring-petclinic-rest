package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.ClinicService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Maps Owner &amp; OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /**
     * A create is flagged with {@code bulkSignupWarning=true} once more than this many owners have
     * already been created on the same registration date.
     */
    private static final int BULK_SIGNUP_THRESHOLD = 80;

    @Autowired
    protected ClinicService clinicService;

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email address is
     * present, otherwise {@code PHONE}.
     */
    protected OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's locality (region) from the city using the fixed city-to-region
     * table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD), returning {@code UNKNOWN} when the
     * city is not in the table.
     */
    protected String locality(Owner owner) {
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
     * The highest membership level derivable on creation. Level 4 is reserved for tenure and is
     * not assigned here.
     */
    private static final int MAX_MEMBERSHIP_LEVEL = 3;

    /**
     * Derives the owner's membership level, a number from 1 to {@value #MAX_MEMBERSHIP_LEVEL}:
     * it starts at 1, gains 1 when an email is present, and gains 1 when the owner has no
     * namesakes (namesakeCount is 0), capped at {@value #MAX_MEMBERSHIP_LEVEL}.
     */
    protected Integer membershipLevel(Owner owner) {
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
     * Flags a bulk-signup day: {@code true} once more than {@link #BULK_SIGNUP_THRESHOLD} other
     * owners already carry this owner's {@code registrationDate}. The owner itself is excluded from
     * the count, so on a create the flag reflects the owners that pre-existed it that day, matching
     * the accumulation the per-day create limit is enforced against. Returns {@code false} when the
     * owner has no registration date.
     */
    protected Boolean bulkSignupWarning(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long othersOnDay = this.clinicService.findAllOwners().stream()
            .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
            .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
            .count();
        return othersOnDay > BULK_SIGNUP_THRESHOLD;
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
