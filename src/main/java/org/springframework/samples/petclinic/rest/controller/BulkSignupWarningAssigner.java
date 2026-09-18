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

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Records, on create, whether the new owner's registration day was already busy enough to
 * warrant a bulk-signup warning.
 * <p>
 * The warning is raised once more than {@link #BULK_SIGNUP_THRESHOLD} owners already carry
 * the new owner's effective, business-day-adjusted registration date. The count reflects the
 * owners stored before this create, so it must be assigned before the owner is saved.
 */
@Component
public class BulkSignupWarningAssigner {

    /** Number of owners that must already share a registration date before the warning is raised. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    private final ClinicService clinicService;

    public BulkSignupWarningAssigner(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Assigns {@code owner}'s bulk-signup warning: {@code true} when more than
     * {@link #BULK_SIGNUP_THRESHOLD} already-stored owners share its registration date,
     * otherwise {@code false}. Call this before the owner is saved so it does not count itself.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        long alreadyRegistered = clinicService.countOwnersByRegistrationDate(owner.getRegistrationDate());
        owner.setBulkSignupWarning(alreadyRegistered > BULK_SIGNUP_THRESHOLD);
    }
}
