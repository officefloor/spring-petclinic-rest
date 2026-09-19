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
 * Builds the canonical API self-link for an owner.
 */
public final class OwnerSelfLink {

    private static final String OWNERS_PATH = "/api/owners/";

    private OwnerSelfLink() {
    }

    /**
     * Compose the owner's self-link as {@code "/api/owners/<id>"}.
     *
     * @param id the owner's id; may be {@code null} for a not-yet-persisted owner
     * @return the self-link, or {@code null} when the id is unknown
     */
    public static String of(Integer id) {
        return id == null ? null : OWNERS_PATH + id;
    }
}
