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
 * The single derived duplicate-detection key for an owner: the normalized telephone, the email
 * (or empty) and the household id, joined with '|'. Two owners are duplicates only when their whole
 * key matches. The household id is not carried on the persisted owner, so it contributes empty here.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String key(Owner owner) {
        String email = owner.getEmail();
        return owner.getTelephone() + "|" + (email == null ? "" : email) + "|";
    }
}
