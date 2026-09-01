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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import tools.jackson.databind.ObjectMapper;

/**
 * Ensures every rejection response (400, 409, 429) leaves the application as an RFC 7807
 * {@code application/problem+json} document carrying {@code type}, {@code title},
 * {@code status} and {@code detail}. Responses a handler already rendered as problem+json
 * are passed through untouched, so this filter only upgrades the plain bodies produced by
 * the per-rule request-body advices. Kept as one small filter so no existing handler has
 * to grow to learn the problem+json format.
 */
@Component
public class ProblemJsonRejectionFilter extends OncePerRequestFilter {

    private static final Set<Integer> REJECTION_STATUSES = Set.of(400, 409, 429);

    private final ObjectMapper objectMapper;

    public ProblemJsonRejectionFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);
        if (needsUpgrade(wrapper)) {
            writeProblemJson(response, wrapper);
        }
        else {
            wrapper.copyBodyToResponse();
        }
    }

    private boolean needsUpgrade(ContentCachingResponseWrapper wrapper) {
        String contentType = wrapper.getContentType();
        return REJECTION_STATUSES.contains(wrapper.getStatus())
            && (contentType == null || !contentType.contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    private void writeProblemJson(HttpServletResponse response, ContentCachingResponseWrapper wrapper)
            throws IOException {
        int status = wrapper.getStatus();
        String reason = HttpStatus.valueOf(status).getReasonPhrase();
        String original = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8).trim();
        Map<String, Object> problem = new LinkedHashMap<>();
        problem.put("type", URI.create("about:blank").toString());
        problem.put("title", reason);
        problem.put("status", status);
        problem.put("detail", original.isBlank() ? reason : original);
        byte[] body = objectMapper.writeValueAsBytes(problem);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }
}
