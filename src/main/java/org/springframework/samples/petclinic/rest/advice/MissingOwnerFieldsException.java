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

package org.springframework.samples.petclinic.rest.advice;

import java.util.List;

/**
 * Thrown when a request to create or update an owner omits, or leaves blank, one or more
 * mandatory fields. The {@link #getMissingFields()} list carries the names of the offending
 * fields so the response can report exactly which values must be supplied.
 */
public class MissingOwnerFieldsException extends RuntimeException {

    private final List<String> missingFields;

    public MissingOwnerFieldsException(List<String> missingFields) {
        super("Missing or blank owner fields: " + missingFields);
        this.missingFields = List.copyOf(missingFields);
    }

    public List<String> getMissingFields() {
        return this.missingFields;
    }
}
