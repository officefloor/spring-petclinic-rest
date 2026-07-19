package org.springframework.samples.petclinic.rest.function.visit;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Builds a Visit referencing its Pet purely by id from the path - matches the original
 * behaviour of not loading (or validating the existence of) the owner or pet first.
 */
@Validated
public class BuildOwnersVisit {

    public void service(@Valid @RequestBody VisitFieldsDto request,
            @PathVariable(name = "petId") Integer petId,
            VisitMapper visitMapper, Out<Visit> built) {
        Visit visit = visitMapper.toVisit(request);
        Pet pet = new Pet();
        pet.setId(petId);
        visit.setPet(pet);
        built.set(visit);
    }
}
