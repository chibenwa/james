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

package org.apache.james.hystrix.rrt;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.james.rrt.api.RecipientRewriteTableException;
import org.junit.Before;
import org.junit.Test;

import org.apache.james.rrt.api.RecipientRewriteTable;

public class HystrixRecipientRewriteTableTest {

    private static final String USER = "user";
    private static final String DOMAIN = "domain";
    private static final String ADDRESS = "address";
    private static final String REAL_DOMAIN = "other";
    private static final String ERROR = "error";
    private static final String MAPPING = "mapping";
    private static final String REGEX = "regex";

    private RecipientRewriteTable mockedRecipientRewirtiteTable;
    private RecipientRewriteTable testee;

    @Before
    public void setUp() throws Exception {
        mockedRecipientRewirtiteTable = mock(RecipientRewriteTable.class);
        testee = new HystrixRecipientRewriteTable(mockedRecipientRewirtiteTable);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RecipientRewriteTableException.class)
    public void recipientRewriteTableExceptionShouldBePropagated() throws  Exception {
        when(mockedRecipientRewirtiteTable.getAllMappings()).thenThrow(RecipientRewriteTableException.class);
        testee.getAllMappings();
    }

    @Test
    public void addAddressMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.addAddressMapping(USER, DOMAIN, ADDRESS);
        verify(mockedRecipientRewirtiteTable).addAddressMapping(USER, DOMAIN, ADDRESS);
    }

    @Test
    public void addAliasDomainMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.addAliasDomainMapping(DOMAIN, REAL_DOMAIN);
        verify(mockedRecipientRewirtiteTable).addAliasDomainMapping(DOMAIN, REAL_DOMAIN);
    }

    @Test
    public void addErrorMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.addErrorMapping(USER, DOMAIN, ERROR);
        verify(mockedRecipientRewirtiteTable).addErrorMapping(USER, DOMAIN, ERROR);
    }

    @Test
    public void addMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.addMapping(USER, DOMAIN, MAPPING);
        verify(mockedRecipientRewirtiteTable).addMapping(USER, DOMAIN, MAPPING);
    }

    @Test
    public void addRegexMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.addRegexMapping(USER, DOMAIN, REGEX);
        verify(mockedRecipientRewirtiteTable).addRegexMapping(USER, DOMAIN, REGEX);
    }

    @Test
    public void getAllMappingsShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.getAllMappings();
        verify(mockedRecipientRewirtiteTable).getAllMappings();
    }

    @Test
    public void getMappingsShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.getMappings(USER, DOMAIN);
        verify(mockedRecipientRewirtiteTable).getMappings(USER, DOMAIN);
    }

    @Test
    public void getUserDomainMappingsShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.getUserDomainMappings(USER, DOMAIN);
        verify(mockedRecipientRewirtiteTable).getUserDomainMappings(USER, DOMAIN);
    }

    @Test
    public void removeAddressMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.removeAddressMapping(USER, DOMAIN, ADDRESS);
        verify(mockedRecipientRewirtiteTable).removeAddressMapping(USER, DOMAIN, ADDRESS);
    }

    @Test
    public void removeAliasDomainMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.removeAliasDomainMapping(DOMAIN, REAL_DOMAIN);
        verify(mockedRecipientRewirtiteTable).removeAliasDomainMapping(DOMAIN, REAL_DOMAIN);
    }

    @Test
    public void removeErrorMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.removeErrorMapping(USER, DOMAIN, ERROR);
        verify(mockedRecipientRewirtiteTable).removeErrorMapping(USER, DOMAIN, ERROR);
    }

    @Test
    public void removeMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.removeMapping(USER, DOMAIN, MAPPING);
        verify(mockedRecipientRewirtiteTable).removeMapping(USER, DOMAIN, MAPPING);
    }

    @Test
    public void removeRegexMappingShouldCallUnderlyingRecipientRewriteTable() throws Exception {
        testee.removeRegexMapping(USER, DOMAIN, REGEX);
        verify(mockedRecipientRewirtiteTable).removeRegexMapping(USER, DOMAIN, REGEX);
    }

    @Test
    public void getAllMappingsShouldFallback() throws Exception {
        when(mockedRecipientRewirtiteTable.getAllMappings()).thenThrow(new RuntimeException());
        assertThat(testee.getAllMappings()).isEmpty();
    }

    @Test
    public void getMappingsShouldFallback() throws Exception {
        when(mockedRecipientRewirtiteTable.getMappings(USER, DOMAIN)).thenThrow(new RuntimeException());
        assertThat(testee.getMappings(USER, DOMAIN)).isEmpty();
    }

    @Test
    public void getUserDomainMappingsShouldFallback() throws Exception {
        when(mockedRecipientRewirtiteTable.getUserDomainMappings(USER, DOMAIN)).thenThrow(new RuntimeException());
        assertThat(testee.getUserDomainMappings(USER, DOMAIN)).isEmpty();
    }

}
