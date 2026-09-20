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

package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Decides whether an owner should carry a bulk-signup warning: the flag is raised once more
 * than {@value #BULK_SIGNUP_THRESHOLD} owners share the owner's registration date, signalling
 * an unusually high volume of same-day registrations. It reads the same per-day registration
 * count the daily-limit rule uses, only with a lower threshold.
 */
@Component
public class BulkSignupWarningEvaluator {

    /** Number of same-day registrations above which the warning is raised. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    private final ClinicService clinicService;

    public BulkSignupWarningEvaluator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param owner the owner whose registration date is checked
     * @return {@code true} when more than {@value #BULK_SIGNUP_THRESHOLD} owners are registered
     *         on the owner's registration date, {@code false} otherwise (including when the owner
     *         or its registration date is not yet known)
     */
    public boolean isWarranted(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return false;
        }
        return clinicService.countOwnersRegisteredOn(owner.getRegistrationDate()) > BULK_SIGNUP_THRESHOLD;
    }
}
