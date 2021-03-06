/*
 * Copyright (c) 2012. Piraso Alvin R. de Leon. All Rights Reserved.
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

package org.piraso.io.util;

import org.piraso.api.JacksonUtils;
import org.piraso.api.entry.MessageEntry;
import org.piraso.api.io.EntryReadEvent;
import org.piraso.io.IOEntry;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertNotNull;

/**
 * Test for {@link IOEntrySerializable} class.
 */
public class IOEntrySerializableTest {
    @Test
    public void testJackson() throws Exception {
        ObjectMapper mapper = JacksonUtils.MAPPER;

        IOEntrySerializable serializable = new IOEntrySerializable(new EntryReadEvent(this, 12l, new MessageEntry("test"), new Date()));

        String str = mapper.writeValueAsString(serializable);

        IOEntrySerializable actual = mapper.readValue(str, IOEntrySerializable.class);
        IOEntry entry = new IOEntry(actual);

        assertNotNull(entry.getEntry());
    }
}
