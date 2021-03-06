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

package org.piraso.server;

import org.piraso.api.IntegerIDGenerator;
import org.piraso.api.PirasoLogger;
import org.apache.commons.logging.Log;
import org.springframework.beans.BeansException;

/**
 * Base class for {@link ContextLoggerBeanProcessor}.
 */
public abstract class AbstractContextLoggerBeanProcessor<T> implements ContextLoggerBeanProcessor<T> {

    private static final Log LOG = PirasoLogger.getProxyEntry();

    private static final IntegerIDGenerator generator = new IntegerIDGenerator();

    private Class<T> clazz;
    private int order;

    public AbstractContextLoggerBeanProcessor(Class<T> clazz) {
        this.clazz = clazz;
        order = generator.next();
    }

    /**
     * {@inheritDoc}
     */
    public abstract T createProxy(T o, String id);

    /**
     * {@inheritDoc}
     */
    public boolean isSupported(Object o) {
        return o != null && clazz.isInstance(o);
    }

    /**
     * {@inheritDoc}
     */
    public int getOrder() {
        return order;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(isSupported(bean)) {
            if(LOG.isInfoEnabled()) {
                LOG.info(String.format(
                        "[PIRASO PROXY ENTRY]: bean=%s, class=%s",
                        beanName,
                        bean.getClass().getName())
                );
            }

            return createProxy((T) bean, beanName);
        }

        return bean;
    }

    /**
     * {@inheritDoc}
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
