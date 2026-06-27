package org.springframework.samples.petclinic.rest.endpoint;

import jakarta.validation.Valid;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Validated
public class VisitEndpoint {

    public void listVisits(
            VisitRepository visitRepository,
            VisitMapper visitMapper,
            ObjectResponse<ResponseEntity<List<VisitDto>>> response) {
        Collection<Visit> visits = visitRepository.findAll();
        if (visits.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(new ArrayList<>(visitMapper.toVisitsDto(visits))));
    }

    public void getVisit(
            @PathVariable(name = "visitId") Integer visitId,
            VisitRepository visitRepository,
            VisitMapper visitMapper,
            ObjectResponse<ResponseEntity<VisitDto>> response) {
        Visit visit = findById(() -> visitRepository.findById(visitId));
        if (visit == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(visitMapper.toVisitDto(visit)));
    }

    public void addVisit(
            @Valid @RequestBody VisitDto visitDto,
            VisitRepository visitRepository,
            VisitMapper visitMapper,
            ObjectResponse<ResponseEntity<VisitDto>> response) {
        Visit visit = visitMapper.toVisit(visitDto);
        visitRepository.save(visit);
        response.send(ResponseEntity.status(201).body(visitMapper.toVisitDto(visit)));
    }

    public void updateVisit(
            @PathVariable(name = "visitId") Integer visitId,
            @Valid @RequestBody VisitFieldsDto visitFieldsDto,
            VisitRepository visitRepository,
            VisitMapper visitMapper,
            ObjectResponse<ResponseEntity<VisitDto>> response) {
        Visit currentVisit = findById(() -> visitRepository.findById(visitId));
        if (currentVisit == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        currentVisit.setDate(visitFieldsDto.getDate());
        currentVisit.setDescription(visitFieldsDto.getDescription());
        visitRepository.save(currentVisit);
        response.send(ResponseEntity.status(204).body(visitMapper.toVisitDto(currentVisit)));
    }

    public void deleteVisit(
            @PathVariable(name = "visitId") Integer visitId,
            VisitRepository visitRepository,
            ObjectResponse<ResponseEntity<VisitDto>> response) {
        Visit visit = findById(() -> visitRepository.findById(visitId));
        if (visit == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        visitRepository.delete(visit);
        response.send(ResponseEntity.noContent().build());
    }

    private static <T> T findById(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            return null;
        }
    }
}
