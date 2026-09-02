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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Rejects a newly created owner whose whole {@link OwnerIdentity#key(Owner) identity key} — the
 * normalized telephone, email and household id — exactly matches an existing owner's. This single
 * key subsumes the former separate telephone, email and household duplicate checks. Updates to an
 * existing owner keep their own identity and are left untouched.
 */
@Aspect
@Component
public class OwnerIdentityUniquenessAspect {

    private final OwnerRepository ownerRepository;

    public OwnerIdentityUniquenessAspect(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Before("execution(* org.springframework.samples.petclinic.service.ClinicService.saveOwner(..)) && args(owner)")
    public void rejectDuplicateIdentity(Owner owner) {
        if (!owner.isNew()) {
            return;
        }
        String identityKey = OwnerIdentity.key(owner);
        boolean taken = ownerRepository.findAll().stream()
            .anyMatch(existing -> identityKey.equals(OwnerIdentity.key(existing)));
        if (taken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another owner already has this identity");
        }
    }
}
