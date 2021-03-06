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

package org.piraso.headless;

import org.piraso.api.entry.Entry;
import org.piraso.headless.restriction.Restriction;
import org.piraso.io.IOEntry;
import org.piraso.io.IOEntryManager;
import org.piraso.io.IOEntryVisitor;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Restriction class instance
 */
public class EntryCriteria {

    private IOEntryManager manager;

    private List<Restriction> restrictions;

    EntryCriteria(IOEntryManager manager) {
        this.manager = manager;
        restrictions = new LinkedList<Restriction>();
    }

    public EntryCriteria add(Restriction restriction) {
        restrictions.add(restriction);

        return this;
    }

    private boolean matches(IOEntry entry) {
        for(Restriction r : restrictions) {
            if(!r.matches(entry.getEntry())) {
                return false;
            }
        }

        return true;
    }

    public Entry uniqueResult() {
        List<Entry> items = list();

        if(CollectionUtils.isEmpty(items)) {
            return null;
        }

        if(CollectionUtils.size(items) > 1) {
            throw new PirasoHeadlessException("Has more than one entries found.");
        }

        return items.iterator().next();
    }

    public Entry firstResult() {
        LinkedList<Entry> items = list();

        if(CollectionUtils.isEmpty(items)) {
            return null;
        }

        return items.getFirst();
    }


    public Entry lastResult() {
        LinkedList<Entry> items = list();

        if(CollectionUtils.isEmpty(items)) {
            return null;
        }

        return items.getLast();
    }

    public LinkedList<Entry> list() {
        final LinkedList<Entry> results = new LinkedList<Entry>();

        try {
            manager.visit(new IOEntryVisitor() {
                public void visit(IOEntry entry) {
                    if(matches(entry)) {
                        results.add(entry.getEntry());
                    }
                }
            });
        } catch (IOException e) {
            throw new PirasoHeadlessException("Error while visiting all piraso entries.", e);
        }

        return results;
    }
}
