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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.james.dnsservice.dnsjava.DNSJavaService;
import org.apache.mailet.Mailet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GuiceGenericLoaderTest {

    private GuiceGenericLoader<Mailet> genericLoader;
    private static Injector injector;

    @BeforeClass
    public static void setUpClass() {
        injector = Guice.createInjector();
    }

    @Before
    public void setUp() {
        genericLoader = new GuiceGenericLoader<>(injector, "org.apache.james.dnsservice.dnsjava");
    }

    @Test
    public void loadShouldRetrieveGivenClassInstanceWhenFullPathIsSpecified() throws Exception {
        assertThat(genericLoader.load("org.apache.james.dnsservice.dnsjava.DNSJavaService")).isInstanceOf(DNSJavaService.class);
    }

    @Test
    public void loadShouldRetrieveGivenClassInstanceFromStandardPackageWhenNoFullPathIsSpecified() throws Exception {
        assertThat(genericLoader.load("DNSJavaService")).isInstanceOf(DNSJavaService.class);
    }

    @Test(expected = RuntimeException.class)
    public void loadShouldThrowWhenWrongFullPathIsSpecified() throws Exception {
        assertThat(genericLoader.load("org.apache.james.dnsservice.dnsjava.NotExist")).isInstanceOf(DNSJavaService.class);
    }

    @Test(expected = RuntimeException.class)
    public void loadShouldThrowWhenWrongNoFullPathIsSpecified() throws Exception {
        assertThat(genericLoader.load("NotExist")).isInstanceOf(DNSJavaService.class);
    }
}
