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

/**
 * Base type for the field-level validation rules that reject a supplied owner field with a
 * 400 Bad Request. Each concrete subclass stands for one invalid field (e.g. a telephone that is
 * not valid E.164 or an address blank after normalization) and supplies the fixed, client-facing
 * {@code detail} that describes the problem, while its {@code message} carries the contextual text
 * used for logging. All subclasses are handled uniformly by {@link ExceptionControllerAdvice} as a
 * 400 Bad Request, so a new field-validation rule only needs a new subclass, not a new handler.
 */
public abstract class InvalidOwnerFieldException extends OwnerRuleException {

    protected InvalidOwnerFieldException(String message, String detail) {
        super(message, detail);
    }
}
