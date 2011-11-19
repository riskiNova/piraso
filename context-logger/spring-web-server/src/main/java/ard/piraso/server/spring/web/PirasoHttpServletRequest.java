/*
 * Copyright (c) 2011. Piraso Alvin R. de Leon. All Rights Reserved.
 *
 * See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Piraso licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ard.piraso.server.spring.web;

import ard.piraso.api.JacksonUtils;
import ard.piraso.api.Preferences;
import ard.piraso.server.PirasoEntryPoint;
import ard.piraso.server.PirasoRequest;
import org.apache.commons.lang.Validate;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static ard.piraso.api.PirasoConstants.*;

/**
 * Http implementation of {@link PirasoEntryPoint} and {@link PirasoRequest}.
 */
public class PirasoHttpServletRequest implements PirasoEntryPoint, PirasoRequest {

    private HttpServletRequest request;

    public PirasoHttpServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getPath() {
        return request.getRequestURI();
    }

    public Preferences getPreferences() {
        Validate.notNull(request.getParameter(PREFERENCES_PARAMETER), "");

        ObjectMapper mapper = JacksonUtils.createMapper();

        try {
            return mapper.readValue(request.getParameter(PREFERENCES_PARAMETER), Preferences.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    public String getWatchedAddr() {
        return request.getParameter(WATCHED_ADDR_PARAMETER);
    }

    public String getActivityUuid() {
        return request.getParameter(ACTIVITY_PARAMETER);
    }
}
