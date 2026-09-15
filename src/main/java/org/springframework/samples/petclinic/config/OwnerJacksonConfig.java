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

package org.springframework.samples.petclinic.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson customization for owner payloads.
 * <p>
 * {@code sharesHousehold} is a write-only hint accepted only when creating an owner (see the
 * {@code OwnerFields} schema); it is never stored, so it has nothing to return. The generated
 * {@link OwnerDto} nevertheless inherits the property from the shared schema, so a mix-in marks
 * it {@link JsonProperty.Access#WRITE_ONLY} to keep it out of owner responses.
 */
@Configuration
public class OwnerJacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer ownerSharesHouseholdWriteOnlyCustomizer() {
        return builder -> builder.addMixIn(OwnerDto.class, OwnerDtoMixin.class);
    }

    /** Hides the write-only {@code sharesHousehold} hint from serialized {@link OwnerDto} responses. */
    abstract static class OwnerDtoMixin {

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        abstract Boolean getSharesHousehold();
    }
}
