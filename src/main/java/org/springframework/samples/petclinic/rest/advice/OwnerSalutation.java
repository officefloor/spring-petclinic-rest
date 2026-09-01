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

package org.springframework.samples.petclinic.rest.advice;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code salutation} from its optional title (MR/MRS/MS/DR):
 * '{@code title lastName}' when a title is supplied, otherwise just the lastName.
 * Kept as a small standalone unit so the response mapper can expose the value without growing.
 */
public final class OwnerSalutation {

    private OwnerSalutation() {
    }

    public static String of(Owner owner) {
        String title = owner.getTitle();
        return title == null || title.isBlank() ? owner.getLastName() : title + " " + owner.getLastName();
    }
}
