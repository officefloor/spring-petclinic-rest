package org.springframework.samples.petclinic.rest.function.visit;

import java.util.List;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

public class ListVisits {

    public void service(VisitRepository visitRepository, VisitMapper visitMapper,
            ObjectResponse<ResponseEntity<List<VisitDto>>> response) {
        List<VisitDto> visits = List.copyOf(visitMapper.toVisitsDto(visitRepository.findAll()));
        if (visits.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(visits));
    }
}
