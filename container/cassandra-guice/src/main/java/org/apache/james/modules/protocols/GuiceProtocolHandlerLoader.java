/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.modules.protocols;

import com.google.common.base.Throwables;
import com.google.inject.Injector;
import org.apache.commons.configuration.Configuration;
import org.apache.james.protocols.api.handler.LifecycleAwareProtocolHandler;
import org.apache.james.protocols.api.handler.ProtocolHandler;
import org.apache.james.protocols.lib.handler.ProtocolHandlerLoader;
import org.apache.james.protocols.lib.lifecycle.InitializingLifecycleAwareProtocolHandler;

public class GuiceProtocolHandlerLoader implements ProtocolHandlerLoader {

    private final Injector injector;

    public GuiceProtocolHandlerLoader(Injector injector) {
        this.injector = injector;
    }

    @Override
    public ProtocolHandler load(String name, Configuration config) throws LoadingException {
        try {
            ProtocolHandler handler = (ProtocolHandler) injector.getInstance(Class.forName(name));
            if (handler instanceof LifecycleAwareProtocolHandler) {
                ((InitializingLifecycleAwareProtocolHandler) handler).init(config);
            }
            return handler;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
