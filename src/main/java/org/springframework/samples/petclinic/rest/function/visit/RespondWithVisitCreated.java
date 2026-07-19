package org.springframework.samples.petclinic.rest.function.visit;

import java.net.URI;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

public class RespondWithVisitCreated {

    public void service(@Val Visit visit, VisitMapper visitMapper, ObjectResponse<ResponseEntity<VisitDto>> response) {
        VisitDto dto = visitMapper.toVisitDto(visit);
        response.send(ResponseEntity.created(URI.create("/api/visits/" + visit.getId())).body(dto));
    }
}
