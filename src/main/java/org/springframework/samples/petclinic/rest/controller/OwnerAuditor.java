/*
 * Copyright 2016-2017 the original author or authors.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Owns the owner-create audit concern: the single place that records the audit trail for a
 * successfully created {@link Owner}.
 *
 * <p>Keeping the audit trail here means every create side-effect that must be observable to an
 * auditor is emitted from one place, in one form, on the dedicated {@code AUDIT} logger — so the
 * registrar is left holding only the create pipeline, and what is audited on create can evolve
 * without threading logging detail back through it.
 */
@Component
public class OwnerAuditor {

    /** Dedicated audit logger; emits the audit trail on successful owner create. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Record the audit trail for an owner that has just been created: emit the audit line naming
     * the owner's id, customer code, registration date, membership level and membership number.
     * Called once, after the owner is persisted, with the owner in its final stored form.
     *
     * @param owner           the freshly persisted owner
     * @param membershipLevel the owner's derived membership level, as computed for this create
     */
    public void ownerCreated(Owner owner, int membershipLevel) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            membershipLevel, owner.getMembershipNumber());
    }
}
