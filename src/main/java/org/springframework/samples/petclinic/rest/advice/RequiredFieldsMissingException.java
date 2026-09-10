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
 * Thrown when a request omits or leaves blank one or more required fields. See
 * {@link FieldValidationException} for how the offending field names are surfaced
 * to the client.
 */
public class RequiredFieldsMissingException extends FieldValidationException {

    public RequiredFieldsMissingException(List<String> fields) {
        super("Required fields missing or blank: " + fields, fields);
    }
}
