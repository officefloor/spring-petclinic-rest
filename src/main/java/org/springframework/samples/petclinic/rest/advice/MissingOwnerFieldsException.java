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

import java.util.List;

/**
 * Thrown when a request to create or update an owner is missing or blank in one or more
 * required fields. Carries the names of the offending fields so they can be reported to
 * the client.
 */
public class MissingOwnerFieldsException extends RuntimeException {

    private final List<String> errors;

    public MissingOwnerFieldsException(List<String> errors) {
        super("Missing or blank owner fields: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return this.errors;
    }
}
