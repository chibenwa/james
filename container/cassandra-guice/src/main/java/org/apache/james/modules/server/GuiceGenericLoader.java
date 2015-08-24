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

package org.apache.james.modules.server;

import com.google.common.base.Throwables;
import com.google.inject.Injector;
import org.apache.mailet.MailetException;

public class GuiceGenericLoader<T> {

    private final Injector injector;
    private final String standardPackage;

    public GuiceGenericLoader(Injector injector, String standardPackage) {
        this.injector = injector;
        this.standardPackage = standardPackage;
    }

    public T load(String name) throws MailetException {
        try {
            Class<T> c = (Class<T>) ClassLoader.getSystemClassLoader().loadClass(constructFullName(name, standardPackage));
            return  injector.getInstance(c);
        } catch (ClassNotFoundException e) {
            throw Throwables.propagate(e);
        }
    }

    private String constructFullName(String name, String standardPackage) {
        if (name.indexOf(".") < 1) {
            return standardPackage + "." + name;
        }
        return name;
    }

}
