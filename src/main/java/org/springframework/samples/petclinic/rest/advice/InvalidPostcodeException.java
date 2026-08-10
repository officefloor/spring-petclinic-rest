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

/**
 * Thrown on create when a supplied postcode is invalid — either it is not four digits or it falls
 * outside the range its city's region allows (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city
 * with no known region accepts any 4-digit postcode. Carries the {@code postcode} field name so the
 * {@link ExceptionControllerAdvice} can report it in the {@code errors} array of a 400 Bad Request
 * response, mirroring {@link InvalidTelephoneException}.
 */
public class InvalidPostcodeException extends RuntimeException {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
