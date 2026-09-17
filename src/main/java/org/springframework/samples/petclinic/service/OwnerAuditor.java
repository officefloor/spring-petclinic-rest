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
package org.springframework.samples.petclinic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Emits audit records for owner life-cycle events to the dedicated {@code AUDIT}
 * logger, keeping audit side-effects separate from the create business logic.
 */
@Component
public class OwnerAuditor {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final OwnerMapper ownerMapper;

    public OwnerAuditor(OwnerMapper ownerMapper) {
        this.ownerMapper = ownerMapper;
    }

    /**
     * Record the successful creation of an owner, capturing its id, customer code,
     * registration date and derived membership number and level.
     */
    public void auditCreated(Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipNumber={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            this.ownerMapper.formatMembershipNumber(owner), this.ownerMapper.resolveMembershipLevel(owner));
    }
}
