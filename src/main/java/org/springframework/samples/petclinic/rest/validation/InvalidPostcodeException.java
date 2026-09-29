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

package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when an owner's supplied postcode is not a 4-digit value, or is out of
 * range for the region derived from the owner's city (NSW 2000-2099, VIC
 * 3000-3099, QLD 4000-4099). Handled by the REST {@code ExceptionControllerAdvice},
 * which translates it into a {@code 400 Bad Request} response.
 */
public class InvalidPostcodeException extends RuntimeException {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
