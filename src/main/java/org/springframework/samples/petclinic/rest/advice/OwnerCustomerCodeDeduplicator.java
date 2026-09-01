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

import java.util.HashSet;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;

/**
 * Makes a freshly-computed customerCode unique: if it already belongs to another owner, appends
 * '-<n>' with the smallest n >= 2 that is free. Kept as its own small unit so the collision rule
 * stays self-contained rather than growing the aspect that computes the base code.
 */
@Component
public class OwnerCustomerCodeDeduplicator {

    private final OwnerRepository owners;

    public OwnerCustomerCodeDeduplicator(OwnerRepository owners) {
        this.owners = owners;
    }

    public String unique(String base) {
        Set<String> used = new HashSet<>();
        for (Owner o : owners.findAll()) {
            used.add(o.getCustomerCode());
        }
        String code = base;
        for (int n = 2; used.contains(code); n++) {
            code = base + "-" + n;
        }
        return code;
    }
}
