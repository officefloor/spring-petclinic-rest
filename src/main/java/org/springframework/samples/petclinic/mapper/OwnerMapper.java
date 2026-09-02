package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
     * Derives the household identifier for the response: a stable value shared by owners with the
     * same last name and address (case- and whitespace-insensitive), so members of one household
     * always report the same id. Non-persistent, so it never affects storage or the request payload.
     */
    @AfterMapping
    default void deriveHouseholdId(Owner owner, @MappingTarget OwnerDto ownerDto) {
        String lastName = owner.getLastName();
        String address = owner.getAddress();
        if (lastName == null || address == null) {
            return;
        }
        String key = (lastName.strip().replaceAll("\\s+", " ") + '\n'
            + address.strip().replaceAll("\\s+", " ")).toLowerCase();
        ownerDto.setHouseholdId(UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString());
    }

    /** Fixed city-to-region table used to derive an owner's locality. */
    Map<String, String> CITY_REGION = Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Derives 'locality' for the response: the canonical region for the owner's city from the fixed
     * {@link #CITY_REGION} table, or 'UNKNOWN' when the city is not listed. Non-persistent, so it
     * never affects storage or the request payload.
     */
    @AfterMapping
    default void deriveLocality(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setLocality(CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN"));
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
