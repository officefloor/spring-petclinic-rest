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
 * Thrown on create when 100 or more owners have already been created today (counted by
 * {@code registrationDate}). The {@link ExceptionControllerAdvice} reports it as a 429 Too Many
 * Requests response, throttling how many owners may be registered in a single day.
 */
public class DailyOwnerLimitExceededException extends RuntimeException {

    public DailyOwnerLimitExceededException(String message) {
        super(message);
    }
}
