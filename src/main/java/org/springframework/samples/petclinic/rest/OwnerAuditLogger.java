/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.samples.petclinic.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.MembershipLevelCalculator;
import org.springframework.samples.petclinic.util.MembershipNumberFormatter;
import org.springframework.stereotype.Component;

/**
 * Emits audit-trail entries for owner lifecycle events to the dedicated {@code AUDIT}
 * logger, keeping audit concerns out of the request-handling controller.
 */
@Component
public class OwnerAuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final OwnerCreatedEventEmitter ownerCreatedEventEmitter;

    public OwnerAuditLogger(OwnerCreatedEventEmitter ownerCreatedEventEmitter) {
        this.ownerCreatedEventEmitter = ownerCreatedEventEmitter;
    }

    /**
     * Record that an owner was successfully created: a human-readable audit line capturing its id,
     * customer code, registration date, membership level and membership number, plus the immutable
     * structured {@link OwnerCreatedEvent}.
     *
     * @param owner the persisted owner
     */
    public void ownerCreated(Owner owner) {
        AUDIT.info(
            "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            MembershipLevelCalculator.effectiveMembershipLevel(owner),
            MembershipNumberFormatter.membershipNumber(owner));
        ownerCreatedEventEmitter.emit(owner);
    }
}
