/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.rest.advice;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.stereotype.Component;

/**
 * Emits the create audit line once the derived membership fields have been populated on the
 * response. Highest precedence makes this after-returning advice run last, so it observes the
 * membershipLevel and membershipNumber set by the other owner-response advices.
 */
@Aspect
@Component
@Order(0)
public class OwnerCreateAuditAdvice {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @AfterReturning(pointcut = "execution(* org.springframework.samples.petclinic.rest.controller.v1."
        + "OwnerRestControllerV1.addOwner(..))", returning = "response")
    public void auditCreatedOwner(ResponseEntity<?> response) {
        if (!(response != null && response.getBody() instanceof OwnerDto owner)) {
            return;
        }
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            owner.getMembershipLevel(), owner.getMembershipNumber());
    }
}
