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

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.james.mailbox.cassandra.CassandraClusterSingleton;
import org.apache.james.mailbox.cassandra.ClusterProvider;
import org.apache.james.mailbox.elasticsearch.*;
import org.apache.james.mailbox.elasticsearch.utils.TestingClientProvider;
import org.apache.james.modules.mailbox.CassandraSessionModule;
import org.apache.james.modules.mailbox.ElasticSearchMailboxModule;
import org.apache.james.modules.protocols.IMAPServerModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.name.Names;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

public class CassandraJamesServerTest {

    private static final CassandraClusterSingleton CASSANDRA = CassandraClusterSingleton.build();
    private static final int IMAP_PORT = 1143; // You need to be root (superuser) to bind to ports under 1024.
    public static final String USER = "user";
    public static final String PASSWORD = "password";

    private TestCassandraJamesServer server;
    private TemporaryFolder temporaryFolder = new TemporaryFolder();
    private EmbeddedElasticSearch embeddedElasticSearch = new EmbeddedElasticSearch(temporaryFolder);

    @Rule
    public RuleChain chain = RuleChain.outerRule(temporaryFolder).around(embeddedElasticSearch);

    private class TestCassandraSessionModule extends CassandraSessionModule {

        @Override
        protected void bindConstants() {
            bind(String.class).annotatedWith(Names.named(ClusterProvider.CASSANDRA_IP)).toInstance(CassandraClusterSingleton.CLUSTER_IP);
            bind(Integer.class).annotatedWith(Names.named(ClusterProvider.CASSANDRA_PORT)).toInstance(CassandraClusterSingleton.CLUSTER_PORT_TEST);
            bind(String.class).annotatedWith(Names.named(ClusterProvider.CASSANDRA_KEYSPACE)).toInstance(CassandraClusterSingleton.KEYSPACE_NAME);
            bind(Integer.class).annotatedWith(Names.named(ClusterProvider.CASSANDRA_REPLICATION_FACTOR)).toInstance(CassandraClusterSingleton.REPLICATION_FACTOR);
        }
    }

    private class TestElasticSearchMailboxModule extends ElasticSearchMailboxModule {

        private final ClientProvider clientProvider;

        public TestElasticSearchMailboxModule(ClientProvider clientProvider) {
            this.clientProvider = clientProvider;
        }

        @Override
        protected void bindClientProvider() {
            bind(ClientProvider.class).toInstance(clientProvider);
        }
    }
    
    private class TestIMAPServerModule extends IMAPServerModule {

        @Override
        protected int imapPort() {
            return IMAP_PORT;
        }
    }
    
    private class TestCassandraJamesServer extends CassandraJamesServer {

        private final ClientProvider clientProvider;

        public TestCassandraJamesServer(EmbeddedElasticSearch embeddedElasticSearch) {
            clientProvider = NodeMappingFactory.applyMapping(
                    IndexCreationFactory.createIndex(new TestingClientProvider(embeddedElasticSearch.getNode()))
            );
        }

        @Override
        protected CassandraSessionModule cassandraSessionModule() {
            return new TestCassandraSessionModule();
        }

        @Override
        protected IMAPServerModule imapServerModule() {
            return new TestIMAPServerModule();
        }

        @Override
        protected ElasticSearchMailboxModule elasticSearchMailboxModule() {
            return new TestElasticSearchMailboxModule(clientProvider);
        }
    }

    @Before
    public void setup() throws Exception {
        CASSANDRA.ensureAllTables();

        server = new TestCassandraJamesServer(embeddedElasticSearch);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        CASSANDRA.clearAllTables();
    }

    @Test (expected = AuthenticationFailedException.class)
    public void connectIMAPServerShouldThrowWhenNoCredentials() throws Exception {
        store().connect();
    }

    private Store store() throws NoSuchProviderException {
        Properties properties = new Properties();
        properties.put("mail.imap.host", "localhost");
        properties.put("mail.imap.port", String.valueOf(IMAP_PORT));
        Session session = Session.getDefaultInstance(properties);
        session.setDebug(true);
        return session.getStore("imap");
    }
}
