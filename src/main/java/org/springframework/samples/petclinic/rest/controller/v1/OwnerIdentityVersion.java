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

package org.springframework.samples.petclinic.rest.controller.v1;

/**
 * Single source of the version-2 owner-identity tag. Version 2 rederives every owner identifier - the
 * {@link HouseholdNormalizer#householdId household id}, the identity key and the member id - by mixing
 * in this fixed {@link #TAG version tag}, so no value produced under version 1 is produced again.
 *
 * <p>The tag is an <em>identifier-only</em> concern: it is folded into the region code that goes
 * <em>inside</em> the member id (see {@link #regionCode(String)}) and into the hashed inputs of the
 * household id and identity key, but it never touches the user-facing {@code locality}, {@code timezone}
 * or {@code ownerSegment}, which continue to use the plain region code (for example {@code NSW}).
 */
final class OwnerIdentityVersion {

    /** The fixed version tag mixed into every version-2 owner identifier. */
    static final String TAG = "V2";

    private OwnerIdentityVersion() {
    }

    /**
     * Returns the version-2 region code used inside the identifiers: the plain region code with the
     * fixed {@link #TAG version tag} appended (for example {@code NSW} becomes {@code NSWV2}). Only the
     * identifiers use this form; the user-facing locality keeps the plain region code.
     *
     * @param plainRegion the plain region code derived from the owner's postcode and city
     * @return the region code carrying the version tag, for use inside the member id
     */
    static String regionCode(String plainRegion) {
        return plainRegion + TAG;
    }

}
