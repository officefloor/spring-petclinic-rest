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
 * Thrown when an owner supplies a 4-digit postcode that is out of range for its city's region
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). Reported as a {@code 400 Bad Request} by the
 * {@link ExceptionControllerAdvice}.
 */
public class InvalidPostcodeException extends RuntimeException {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
