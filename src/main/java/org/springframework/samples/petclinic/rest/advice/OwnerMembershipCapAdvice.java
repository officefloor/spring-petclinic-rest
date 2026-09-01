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

import java.util.OptionalInt;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Caps a returned owner's {@code membershipLevel} at one above the highest membershipLevel among the
 * household members that already existed when it was created — the owners sharing its
 * {@code householdId} with a lower id. With no earlier household member the natural level stands.
 * Kept as its own small advice so this rule stays a self-contained unit.
 */
@ControllerAdvice
public class OwnerMembershipCapAdvice implements ResponseBodyAdvice<Object> {

    private final ClinicService clinicService;

    public OwnerMembershipCapAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof OwnerDto owner && owner.getMembershipLevel() != null) {
            OptionalInt ceiling = householdCeiling(owner);
            if (ceiling.isPresent()) {
                owner.setMembershipLevel(Math.min(owner.getMembershipLevel(), ceiling.getAsInt()));
            }
        }
        return body;
    }

    private OptionalInt householdCeiling(OwnerDto owner) {
        return clinicService.findAllOwners().stream()
            .filter(other -> !Boolean.TRUE.equals(other.getDeleted()))
            .filter(other -> other.getId() != null && owner.getId() != null && other.getId() < owner.getId())
            .filter(other -> owner.getHouseholdId()
                .equals(OwnerIdentityKey.householdId(other.getLastName(), other.getPostcode())))
            .mapToInt(other -> OwnerMembershipLevel.of(other) + 1)
            .max();
    }
}
