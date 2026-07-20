package org.springframework.samples.petclinic.rest.function.vet;

import java.util.List;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.rest.dto.VetDto;

public class ListVets {

    public void service(VetRepository vetRepository, VetMapper vetMapper,
            ObjectResponse<ResponseEntity<List<VetDto>>> response) {
        List<VetDto> vets = List.copyOf(vetMapper.toVetDtos(vetRepository.findAll()));
        if (vets.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(vets));
    }
}
