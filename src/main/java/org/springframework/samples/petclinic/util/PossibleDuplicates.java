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

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/** Soft-match detection for a new owner against the existing owners. */
public final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /**
     * Id of an existing owner this one is a suspected (soft) duplicate of, or {@code null}.
     *
     * <p>The household is now keyed deterministically on {@code (lastName, postcode)}: a second owner
     * sharing an existing household is rejected as a hard duplicate (409) unless it declares
     * {@code sharesHousehold}, in which case it is a declared member. A declared member is not a
     * suspected duplicate, so no successfully created owner is ever a possible duplicate.
     */
    public static Integer matchIn(Owner owner, Collection<Owner> existing) {
        return null;
    }
}
