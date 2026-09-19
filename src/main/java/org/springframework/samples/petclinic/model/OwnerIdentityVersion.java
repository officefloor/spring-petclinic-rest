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
package org.springframework.samples.petclinic.model;

/**
 * The version-2 owner-identity algorithm: the single place that knows how the fixed {@code V2}
 * version tag is mixed into the values derived <em>inside</em> the owner's identifiers (the region
 * code that leads the {@code memberId}, and the hash inputs of the {@code memberId},
 * {@code identityKey} and {@code householdId}). Mixing the tag in guarantees every version-2
 * identifier differs from its version-1 counterpart and never reproduces a version-1 value.
 *
 * <p>The tag is confined to the identifiers: user-facing values that are not identifiers — the
 * {@code locality}, the {@code timezone} and the region the owner segment derives from — stay the
 * plain region code (for example {@code NSW}).
 */
public final class OwnerIdentityVersion {

    /** The current owner-identity algorithm version tag, mixed into every derived identifier. */
    public static final String TAG = "V2";

    private OwnerIdentityVersion() {
    }

    /**
     * The region code used inside the identifiers: the plain region with the version tag appended
     * (for example {@code NSW} becomes {@code NSWV2}). Only the identifiers use this tagged form.
     *
     * @param region the plain region code (the owner's locality)
     * @return the region code used inside the identifiers
     */
    public static String identityRegion(String region) {
        return region + TAG;
    }

    /**
     * Mix the version tag into a hash input as a trailing, delimited segment, so the digest of a
     * version-2 identifier differs from the version-1 digest computed over the same core.
     *
     * @param hashInput the version-1 core hashed to derive an identifier
     * @return the version-2 hash input carrying the version tag
     */
    public static String taggedInput(String hashInput) {
        return hashInput + "|" + TAG;
    }
}
