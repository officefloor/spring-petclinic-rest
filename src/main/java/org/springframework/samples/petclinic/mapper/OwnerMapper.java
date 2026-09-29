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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    /** Fixed city-to-region lookup used to derive an owner's locality. */
    Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    OwnerDto toOwnerDto(Owner owner);

    /**
     * Fills the presentation-only {@code displayName} as {@code "LastName, FirstName"}
     * from the stored names, once MapStruct has mapped the remaining fields.
     */
    @AfterMapping
    default void addDisplayName(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setDisplayName(owner.getLastName() + ", " + owner.getFirstName());
    }

    /**
     * Fills the presentation-only {@code initials} as the upper-cased first letters of
     * firstName and lastName, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    @AfterMapping
    default void addInitials(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setInitials(
            Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".");
    }

    /**
     * Fills the presentation-only {@code membershipNumber} as {@code '<customerCode>-M<YY>'}
     * where YY is the last two digits of the registrationDate year. Left null when either
     * source field is missing.
     */
    @AfterMapping
    default void addMembershipNumber(Owner owner, @MappingTarget OwnerDto ownerDto) {
        if (owner.getCustomerCode() != null && owner.getRegistrationDate() != null) {
            String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
            ownerDto.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
        }
    }

    /**
     * Fills the {@code membershipLevel} from the owner's domain-computed
     * {@link Owner#getMembershipLevel()}.
     */
    @AfterMapping
    default void addMembershipLevel(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setMembershipLevel(owner.getMembershipLevel());
    }

    /**
     * Fills the {@code contactPreference} as {@code EMAIL} when the owner has an email
     * address, otherwise {@code PHONE}.
     */
    @AfterMapping
    default void addContactPreference(Owner owner, @MappingTarget OwnerDto ownerDto) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        ownerDto.setContactPreference(
            hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE);
    }

    /**
     * Fills the presentation-only {@code locality} as the canonical region derived from
     * the owner's city via the fixed {@link #CITY_REGION} table, or {@code 'UNKNOWN'}
     * when the city is not in the table.
     */
    @AfterMapping
    default void addLocality(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setLocality(CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN"));
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
