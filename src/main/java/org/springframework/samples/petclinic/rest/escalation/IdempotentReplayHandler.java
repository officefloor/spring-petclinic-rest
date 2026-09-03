package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class IdempotentReplayHandler {

    public void handle(@Parameter IdempotentReplayException ex, ObjectResponse<OwnerDto> response) {
        response.send(ex.getOwner());
    }
}
