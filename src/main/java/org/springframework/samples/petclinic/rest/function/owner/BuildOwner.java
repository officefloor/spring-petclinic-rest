package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

public class BuildOwner {

    public void service(@RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built)
            throws MissingOwnerFieldsException {
        Map<String, String> required = new LinkedHashMap<>();
        required.put("firstName", request.getFirstName());
        required.put("lastName", request.getLastName());
        required.put("address", request.getAddress());
        required.put("city", request.getCity());
        required.put("telephone", request.getTelephone());
        List<String> missing = required.entrySet().stream()
                .filter(e -> e.getValue() == null || e.getValue().isBlank())
                .map(Map.Entry::getKey).toList();
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        Owner owner = ownerMapper.toOwner(request);
        String telephone = owner.getTelephone().replaceAll("\\D", "");
        if (telephone.length() != 10) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        owner.setTelephone(telephone);
        built.set(owner);
    }
}
