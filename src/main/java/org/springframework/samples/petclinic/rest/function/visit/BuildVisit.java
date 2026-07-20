package org.springframework.samples.petclinic.rest.function.visit;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Builds a top-level visit, taking its pet association from the request body's petId - unlike
 * {@link BuildOwnersVisit}, which takes the pet from the path.
 */
@Validated
public class BuildVisit {

    public void service(@Valid @RequestBody VisitDto request, VisitMapper visitMapper, Out<Visit> built) {
        built.set(visitMapper.toVisit(request));
    }
}
