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
package org.apache.james;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.MiniHBaseCluster;
import org.apache.james.domainlist.hbase.HBaseDomainListTest;
import org.apache.james.system.hbase.TablePool;
import org.apache.james.system.hbase.TablePoolTest;
import org.apache.james.user.hbase.HBaseUsersRepositoryTest;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Integration Tests for the HBase persistence implementations.
 * 
 * Launches a Mini HBase Cluster and run the others tests
 * via a JUnit Test Suite.
 * 
 * HBaseRecipientRewriteTableTest temporary desactivated.
 */
@RunWith(Suite.class)
@SuiteClasses({
    TablePoolTest.class,
    HBaseDomainListTest.class,
//    HBaseRecipientRewriteTableTest.class,
    HBaseUsersRepositoryTest.class
  })
public class JamesServerHBaseIntegrationTest {

    /**
     * The Logger.
     */
    private static Logger logger = Logger.getLogger(JamesServerHBaseIntegrationTest.class);
    
    /**
     * In Memory HBase Mini Cluster.
     */
    private static MiniHBaseCluster hbaseCluster;
    
    /**
     * Launches the HBase Mini Cluster before the tests.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        HBaseTestingUtility htu = new HBaseTestingUtility();
        htu.getConfiguration().setBoolean("dfs.support.append", true);
        try {
            hbaseCluster = htu.startMiniCluster();
        } 
        catch (Exception e) {
            logger.error("Exception when starting HBase Mini Cluster", e);
        }
        TablePool.getInstance(getConfiguration());
    }
    
    /**
     * Shutdowns the HBase Mini Cluster after the tests.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
       if (hbaseCluster != null) {
           hbaseCluster.shutdown();
       }
    }
    
    /**
     * @return the HBase Configuration
     */
    public static Configuration getConfiguration() {
        if (hbaseCluster == null) {
            throw new IllegalStateException("Please instanciate HBaseTestingUtility before invoking this method");
        }
        return hbaseCluster.getConfiguration();
    }

}
