package org.springframework.samples.petclinic.rest.function.visit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;

public class ApplyVisitFields {

    public void service(@Val Visit visit, @Val VisitFieldsDto request) {
        visit.setDate(request.getDate());
        visit.setDescription(request.getDescription());
    }
}
