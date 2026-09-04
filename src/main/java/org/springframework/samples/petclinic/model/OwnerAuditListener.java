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
package org.springframework.samples.petclinic.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.PostPersist;

/**
 * Emits an audit trail entry to the dedicated {@code AUDIT} logger whenever an
 * owner is first persisted, recording the assigned id, customer code and
 * registration date.
 */
public class OwnerAuditListener {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @PostPersist
    void onCreate(Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            MembershipLevel.of(MembershipLevel.points(owner.getNamesakeCount(), owner.getEmail(),
                owner.getHouseholdMemberCount(), owner.getRegistrationDate())),
            owner.getMembershipNumber());
    }
}
