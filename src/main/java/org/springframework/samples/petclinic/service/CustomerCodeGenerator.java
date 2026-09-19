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

import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code customerCode}, formatted {@code <LAST3>-<NNNN>} where
 * {@code LAST3} is the upper-cased first three letters of the last name and
 * {@code NNNN} is a global, four-digit zero-padded sequence number (e.g. {@code SMI-0007}).
 */
@Component
public class CustomerCodeGenerator {

    private static final int PREFIX_LENGTH = 3;

    /**
     * Build the customer code for an owner.
     *
     * @param lastName the owner's last name; its first three letters form the prefix
     * @param sequence the global sequence number placed in the code (zero-padded to four digits)
     * @return the formatted customer code, e.g. {@code SMI-0007}
     */
    public String generate(String lastName, long sequence) {
        String upper = lastName.toUpperCase(Locale.ROOT);
        String prefix = upper.substring(0, Math.min(PREFIX_LENGTH, upper.length()));
        return String.format("%s-%04d", prefix, sequence);
    }
}
