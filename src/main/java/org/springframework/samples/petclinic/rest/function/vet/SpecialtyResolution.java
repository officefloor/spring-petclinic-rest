package org.springframework.samples.petclinic.rest.function.vet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

/**
 * A vet's specialties arrive by name only (see {@code VetDto.specialties}), so before saving,
 * each named specialty must be resolved to its managed entity - otherwise Hibernate would try
 * to insert duplicate specialty rows instead of reusing the existing ones.
 *
 * <p>Resolution works from the request's names directly, never from a transient {@link Specialty}
 * built off the DTO: attaching a transient entity to an already-persistent {@code Vet} (the
 * update path) triggers a Hibernate auto-flush on the next query in the same transaction, which
 * fails because that transient instance isn't saved yet.
 */
final class SpecialtyResolution {

    private SpecialtyResolution() {
    }

    static List<Specialty> resolveByName(List<SpecialtyDto> requested, SpecialtyRepository specialtyRepository) {
        Set<String> names = requested.stream().map(SpecialtyDto::getName).collect(Collectors.toSet());
        return names.isEmpty() ? List.of() : specialtyRepository.findSpecialtiesByNameIn(names);
    }
}
