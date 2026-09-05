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
package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's overall risk flag. It trips when the owner is a possible duplicate,
 * its email domain is disposable-adjacent, or its city is over its soft capacity.
 */
public final class RiskFlag {

    private RiskFlag() {
    }

    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || Boolean.TRUE.equals(owner.getCapacityWarning())
            || DisposableEmailDomain.isDisposableAdjacent(owner.getEmail());
    }
}
