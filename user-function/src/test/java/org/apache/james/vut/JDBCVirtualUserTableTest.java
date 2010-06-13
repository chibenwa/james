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



package org.apache.james.vut;

import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.james.api.vut.management.InvalidMappingException;
import org.apache.james.impl.vut.AbstractVirtualUserTable;
import org.apache.james.test.mock.james.MockFileSystem;
import org.apache.james.test.util.Util;

public class JDBCVirtualUserTableTest extends AbstractVirtualUserTableTest {
    
    /**
     * @see org.apache.james.vut.AbstractVirtualUserTableTest#getVirtalUserTable()
     */
    protected AbstractVirtualUserTable getVirtalUserTable() throws Exception {
        JDBCVirtualUserTable mr = new JDBCVirtualUserTable();
        mr.setLog(new SimpleLog("MockLog"));
        mr.setDataSourceSelector(Util.getDataSourceSelector());
        mr.setDNSService(setUpDNSServer());
        mr.setFileSystem(new MockFileSystem());
        DefaultConfigurationBuilder defaultConfiguration = new DefaultConfigurationBuilder();
        defaultConfiguration.addProperty("[@destinationURL]","db://maildb/VirtualUserTable");
        defaultConfiguration.addProperty("sqlFile","file://conf/sqlResources.xml");
        mr.configure(defaultConfiguration);
        mr.init();
        return mr;
    }    
    
    /**
     * @see org.apache.james.vut.AbstractVirtualUserTableTest#addMapping(java.lang.String, java.lang.String, java.lang.String, int)
     */
    protected boolean addMapping(String user, String domain, String mapping, int type) throws InvalidMappingException {
        if (type == ERROR_TYPE) {
            return virtualUserTable.addErrorMapping(user, domain, mapping);
        } else if (type == REGEX_TYPE) {
            return virtualUserTable.addRegexMapping(user, domain, mapping);
        } else if (type == ADDRESS_TYPE) {
            return virtualUserTable.addAddressMapping(user, domain, mapping);
        } else if (type == ALIASDOMAIN_TYPE) {
            return virtualUserTable.addAliasDomainMapping(domain, mapping);
        } else {
            return false;
        }
    }

    /**
     * @see org.apache.james.vut.AbstractVirtualUserTableTest#removeMapping(java.lang.String, java.lang.String, java.lang.String, int)
     */
    protected boolean removeMapping(String user, String domain, String mapping, int type) throws InvalidMappingException {
        if (type == ERROR_TYPE) {
            return virtualUserTable.removeErrorMapping(user, domain, mapping);
        } else if (type == REGEX_TYPE) {
            return virtualUserTable.removeRegexMapping(user, domain, mapping);
        } else if (type == ADDRESS_TYPE) {
            return virtualUserTable.removeAddressMapping(user, domain, mapping);
        } else if (type == ALIASDOMAIN_TYPE) {
            return virtualUserTable.removeAliasDomainMapping(domain, mapping);
        } else {
            return false;
        }
    }
    
}