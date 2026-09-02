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

import java.time.LocalDate;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.BusinessDay;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Rejects creating an owner once 100 or more owners have already been registered today,
 * counting existing owners by their {@code registrationDate}.
 */
@Aspect
@Component
public class OwnerDailyLimitAdvice {

    private static final int DAILY_LIMIT = 100;

    private final ClinicService clinicService;

    public OwnerDailyLimitAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Before("execution(* org.springframework.samples.petclinic.rest.controller.v1.OwnerRestControllerV1.addOwner(..))")
    public void rejectOverDailyLimit() {
        LocalDate today = BusinessDay.onOrNextBusinessDay(LocalDate.now());
        long createdToday = clinicService.findAllOwners().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        if (createdToday >= DAILY_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "The maximum number of owners for today has already been reached");
        }
    }
}
