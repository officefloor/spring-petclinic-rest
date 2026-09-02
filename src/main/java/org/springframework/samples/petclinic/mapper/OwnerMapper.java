package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Household;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
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

    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Derives the household identifier for the response: the deterministic {@link Household#id} over
     * the owner's last name and postcode, so members of one household always report the same id.
     * Non-persistent, so it never affects storage or the request payload.
     */
    @AfterMapping
    default void deriveHouseholdId(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setHouseholdId(Household.id(owner.getLastName(), owner.getPostcode()));
    }

    /**
     * Derives 'identityKey' for the response: the single duplicate-detection key from
     * {@link OwnerIdentity#key(Owner)}. Non-persistent, so it never affects storage or the request payload.
     */
    @AfterMapping
    default void deriveIdentityKey(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setIdentityKey(OwnerIdentity.key(owner));
    }

    /**
     * Derives 'locality' for the response: the REGION segment of the owner's {@code customerCode}
     * (the '<REGION>-<HASH8>' identity), falling back to {@link LocalityResolver#resolve} when no
     * customerCode is present. Non-persistent, so it never affects storage or the request payload.
     */
    @AfterMapping
    default void deriveLocality(Owner owner, @MappingTarget OwnerDto ownerDto) {
        String code = owner.getCustomerCode();
        int dash = code == null ? -1 : code.indexOf('-');
        ownerDto.setLocality(dash > 0 ? code.substring(0, dash)
            : LocalityResolver.resolve(owner.getCity(), owner.getPostcode()));
    }

    /** Fixed region-to-timezone table, as IANA names. */
    java.util.Map<String, String> REGION_TIMEZONE = java.util.Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derives 'timezone' for the response: the IANA name for the owner's region (the already-derived
     * {@code locality}) from {@link #REGION_TIMEZONE}, left unset when the region has no entry.
     * Non-persistent, so it never affects storage or the request payload.
     */
    @AfterMapping
    default void deriveTimezone(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setTimezone(REGION_TIMEZONE.get(ownerDto.getLocality()));
    }

    /**
     * Derives 'contactPreference' for the response: 'EMAIL' when the owner has an email address,
     * otherwise 'PHONE'. Non-persistent, so it never affects storage or the request payload.
     */
    @AfterMapping
    default void deriveContactPreference(Owner owner, @MappingTarget OwnerDto ownerDto) {
        String email = owner.getEmail();
        ownerDto.setContactPreference(email == null || email.isBlank() ? "PHONE" : "EMAIL");
    }

    /**
     * Derives 'ageBand' for the response from the owner's birth date, measured against the
     * registration date: 'MINOR' when under 18, 'ADULT' for 18-64 and 'SENIOR' for 65+. Left
     * unset when no birth date is supplied. Non-persistent, so it never affects storage.
     */
    @AfterMapping
    default void deriveAgeBand(Owner owner, @MappingTarget OwnerDto ownerDto) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return;
        }
        int years = java.time.Period.between(birthDate, owner.getRegistrationDate()).getYears();
        ownerDto.setAgeBand(years < 18 ? "MINOR" : years < 65 ? "ADULT" : "SENIOR");
    }

    /**
     * Derives 'telephoneDisplay' for the response: the stored E.164 'telephone' formatted for
     * humans by {@link org.springframework.samples.petclinic.util.TelephoneDisplay#format}. Raw
     * 'telephone' is left untouched. Non-persistent, so it never affects storage or the request payload.
     */
    @AfterMapping
    default void deriveTelephoneDisplay(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setTelephoneDisplay(
            org.springframework.samples.petclinic.util.TelephoneDisplay.format(owner.getTelephone()));
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
