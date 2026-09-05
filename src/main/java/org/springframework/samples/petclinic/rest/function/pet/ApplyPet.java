package org.springframework.samples.petclinic.rest.function.pet;

import java.time.LocalDate;

import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

/**
 * Applies the editable pet fields (name, birth date, type) to a {@link Pet}. Shared by the two
 * apply steps that bind different request DTOs ({@code PetDto} and {@code PetFieldsDto}) yet set
 * the same fields identically, so the field mapping lives here rather than being copied into each.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without tripping the
 * one-public-method-per-function rule.
 */
final class ApplyPet {

    private ApplyPet() {
    }

    static void apply(Pet pet, String name, LocalDate birthDate, PetTypeDto type, PetMapper petMapper) {
        pet.setBirthDate(birthDate);
        pet.setName(name);
        pet.setType(petMapper.toPetType(type));
    }
}
