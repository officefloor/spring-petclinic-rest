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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Emits an audit trail entry on the dedicated {@code AUDIT} logger whenever a new owner is
 * successfully created, carrying the assigned id, customerCode and registrationDate. Kept as
 * its own small aspect so this rule stays a self-contained unit rather than growing the
 * controller or service.
 */
@Aspect
@Component
public class OwnerAuditCreationAdvice {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @Around("execution(* org.springframework.samples.petclinic.service.ClinicService.saveOwner(..)) && args(owner)")
    public Object auditCreation(ProceedingJoinPoint joinPoint, Owner owner) throws Throwable {
        boolean created = owner.isNew();
        Object result = joinPoint.proceed();
        if (created) {
            AUDIT.info("owner created id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
        }
        return result;
    }
}
