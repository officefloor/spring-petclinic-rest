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

package org.springframework.samples.petclinic.rest.signup;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.BusinessDay;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Decides whether the bulk-signup warning applies: it does once more than
 * {@value #BULK_SIGNUP_THRESHOLD} owners have already been created on the current business day.
 */
@Component
public class BulkSignupWarningEvaluator {

    static final long BULK_SIGNUP_THRESHOLD = 80;

    private final ClinicService clinicService;

    public BulkSignupWarningEvaluator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @return {@code true} when more than {@value #BULK_SIGNUP_THRESHOLD} owners have already been
     * registered on today's business day (the same business day newly created owners are stored
     * under), {@code false} otherwise.
     */
    public boolean isBulkSignupInEffect() {
        LocalDate today = BusinessDay.onOrAfter(LocalDate.now());
        return this.clinicService.countOwnersRegisteredOn(today) > BULK_SIGNUP_THRESHOLD;
    }
}
