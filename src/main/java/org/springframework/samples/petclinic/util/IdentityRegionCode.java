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

/**
 * Derives the version-2 region code embedded <em>inside</em> owner identifiers (the {@code memberId}).
 *
 * <p>It mixes the fixed {@link IdentityVersion#TAG identity version tag} into the plain canonical
 * region so the code that goes into an identifier differs from the version-1 region and from the
 * user-facing {@code locality}, which stays the untagged plain region (for example {@code "NSW"}).
 */
public final class IdentityRegionCode {

    private IdentityRegionCode() {
    }

    /**
     * Compute the version-2 region code for the given plain region by appending the version tag,
     * for example {@code "NSW"} becomes {@code "NSWV2"}.
     *
     * @param region the plain canonical region (locality), for example {@code "NSW"}
     * @return the version-2 region code carrying the version tag
     */
    public static String v2(String region) {
        return region + IdentityVersion.TAG;
    }
}
