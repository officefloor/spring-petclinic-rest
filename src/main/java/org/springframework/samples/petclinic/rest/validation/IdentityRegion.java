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

package org.springframework.samples.petclinic.rest.validation;

/**
 * Derives the region code used <em>inside</em> the owner's version-2 identifiers — the member id,
 * household id and identity key. It rederives the plain region code by mixing in the fixed
 * {@link #VERSION_TAG version tag}, so every identifier changes and no value minted under version 1
 * is ever produced again.
 *
 * <p>This is deliberately separate from the plain region code (the user-facing {@code locality},
 * {@code timezone} and the owner segment's derived region), which must stay untagged: the version
 * tag appears only within identifiers.
 */
public final class IdentityRegion {

    /** The fixed version tag mixed into every version-2 identifier's region code. */
    public static final String VERSION_TAG = "V2";

    private IdentityRegion() {
    }

    /**
     * Return the version-2 region code for the given plain region: the {@link #VERSION_TAG} followed
     * by the plain region (e.g. {@code "NSW"} becomes {@code "V2NSW"}). Because the plain region is
     * never itself tag-prefixed, the result never collides with a version-1 region code.
     *
     * @param plainRegion the plain region code (see
     *                    {@link org.springframework.samples.petclinic.model.RegionResolver RegionResolver})
     * @return the version-2 region code embedded in identifiers
     */
    public static String of(String plainRegion) {
        return VERSION_TAG + (plainRegion == null ? "" : plainRegion);
    }
}
