/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;

/**
 * Produces the customer code assigned to a new {@link Owner}.
 *
 * <p>The code is formatted {@code '<LAST3>-<NNNN>'}, where {@code LAST3} is the
 * upper-cased first three letters of the owner's last name and {@code NNNN} is a
 * 4-digit zero-padded global sequence equal to one more than the current number
 * of owners (e.g. {@code 'SMI-0007'}).
 */
@Component
public class CustomerCodeGenerator {

    private static final int PREFIX_LENGTH = 3;

    private final OwnerRepository ownerRepository;

    public CustomerCodeGenerator(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * Generate the customer code for the given owner based on its last name and
     * the current owner count.
     *
     * @param owner the owner being registered
     * @return the {@code '<LAST3>-<NNNN>'} customer code
     */
    public String generate(Owner owner) {
        long sequence = ownerRepository.count() + 1;
        return String.format("%s-%04d", prefix(owner.getLastName()), sequence);
    }

    /** The upper-cased first three letters of the given last name. */
    private static String prefix(String lastName) {
        StringBuilder prefix = new StringBuilder(PREFIX_LENGTH);
        for (int i = 0; i < lastName.length() && prefix.length() < PREFIX_LENGTH; i++) {
            char c = lastName.charAt(i);
            if (Character.isLetter(c)) {
                prefix.append(Character.toUpperCase(c));
            }
        }
        return prefix.toString();
    }
}
