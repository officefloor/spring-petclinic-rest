package org.springframework.samples.petclinic.rest.function.visit;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

public class RespondWithVisit {

    public void service(@Val Visit visit, VisitMapper visitMapper, ObjectResponse<VisitDto> response) {
        response.send(visitMapper.toVisitDto(visit));
    }
}
