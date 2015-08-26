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

package org.apache.james.hystrix.domainlist;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.james.domainlist.api.DomainList;
import org.apache.james.domainlist.api.DomainListException;
import org.junit.Before;
import org.junit.Test;

public class HystrixDomainListTest {

    public static final String DOMAIN = "domain";
    private DomainList mockedDomainList;
    private DomainList testee;

    @Before
    public void setUp() {
        mockedDomainList = mock(DomainList.class);
        testee = new HystrixDomainList(mockedDomainList);
    }

    @Test
    public void addDomainShouldCallUnderlyingDomainList() throws Exception {
        testee.addDomain(DOMAIN);
        verify(mockedDomainList).addDomain(DOMAIN);
    }

    @Test
    public void containsDomainShouldCallUnderlyingDomainList() throws Exception {
        testee.containsDomain(DOMAIN);
        verify(mockedDomainList).containsDomain(DOMAIN);
    }

    @Test
    public void getDefaultDomainShouldCallUnderlyingDomainList() throws Exception {
        testee.getDefaultDomain();
        verify(mockedDomainList).getDefaultDomain();
    }

    @Test
    public void getDomainsShouldCallUnderlyingDomainList() throws Exception {
        testee.getDomains();
        verify(mockedDomainList).getDomains();
    }

    @Test
    public void removeDomainShouldCallUnderlyingDomainList() throws Exception {
        testee.removeDomain(DOMAIN);
        verify(mockedDomainList).removeDomain(DOMAIN);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getDomainShouldFallback() throws Exception {
        when(mockedDomainList.getDomains()).thenThrow(RuntimeException.class);
        when(mockedDomainList.getDefaultDomain()).thenAnswer(invocationOnMock -> DOMAIN);
        assertThat(testee.getDomains()).containsExactly(DOMAIN);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void containsDomainShouldFallback() throws Exception {
        when(mockedDomainList.containsDomain(DOMAIN)).thenThrow(RuntimeException.class);
        when(mockedDomainList.getDefaultDomain()).thenAnswer(invocationOnMock -> DOMAIN);
        assertThat(testee.containsDomain(DOMAIN)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = DomainListException.class)
    public void domainListExceptionShouldBePropagated() throws Exception {
        when(mockedDomainList.containsDomain(DOMAIN)).thenThrow(DomainListException.class);
        testee.containsDomain(DOMAIN);
    }
}
