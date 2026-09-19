/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.samples.petclinic.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.MembershipLevelFormatter;
import org.springframework.samples.petclinic.util.MembershipNumberFormatter;
import org.springframework.samples.petclinic.util.MembershipPointsFormatter;
import org.springframework.stereotype.Component;

/**
 * Emits audit records for owner lifecycle events to the dedicated {@code AUDIT} logger,
 * keeping audit-trail concerns out of the business logic that triggers them.
 */
@Component
public class OwnerAuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /**
     * Record that an owner was successfully created, capturing its id, customer code,
     * registration date, membership level and membership number.
     *
     * @param owner the persisted owner (with its generated id) to audit
     */
    public void ownerCreated(Owner owner) {
        AUDIT.info(
            "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            membershipLevel(owner), membershipNumber(owner));
    }

    private static int membershipLevel(Owner owner) {
        int points = MembershipPointsFormatter.format(owner.getNamesakeCount(), owner.getEmail(),
            owner.getHouseholdSize(), owner.getRegistrationDate());
        return MembershipLevelFormatter.format(points);
    }

    private static String membershipNumber(Owner owner) {
        return MembershipNumberFormatter.format(owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
