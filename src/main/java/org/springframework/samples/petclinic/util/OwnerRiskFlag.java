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

package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}, a single boolean that summarises whether the owner warrants
 * closer review. It is {@code true} when any risk signal already recorded on the owner holds: the
 * owner is a possible (soft) duplicate, their email domain is
 * {@link DisposableEmailDomains#isDisposableAdjacent(String) disposable-adjacent}, or the owner was
 * created in a city that was over its soft capacity (the capacity warning threshold).
 */
public final class OwnerRiskFlag {

    private OwnerRiskFlag() {
    }

    /**
     * Whether the owner should carry a risk flag.
     *
     * @param owner the owner to assess
     * @return {@code true} when the owner is a possible duplicate, has a disposable-adjacent email
     * domain, or is over the city's soft capacity; {@code false} otherwise
     */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || DisposableEmailDomains.isDisposableAdjacent(owner.getEmail())
            || Boolean.TRUE.equals(owner.getCapacityWarning());
    }
}
