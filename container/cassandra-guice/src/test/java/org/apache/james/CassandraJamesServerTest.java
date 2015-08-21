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

import javax.mail.*;

import org.apache.james.mailbox.cassandra.CassandraClusterSingleton;
import org.apache.james.mailbox.cassandra.ClusterProvider;
import org.apache.james.mailbox.elasticsearch.*;
import org.apache.james.mailbox.elasticsearch.utils.TestingClientProvider;
import org.apache.james.modules.mailbox.CassandraSessionModule;
import org.apache.james.modules.mailbox.ElasticSearchMailboxModule;
import org.apache.james.modules.protocols.*;
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
    private static final int IMAP_PORT_SSL = 1993;
    private static final int POP3_PORT = 1110;
    public static final int SMTP_PORT = 10025;
    public static final int LMTP_PORT = 10024;

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

    private class TestLMTPServerModule extends LMTPServerModule {

        @Override
        protected int lmtpPort() {
            return LMTP_PORT;
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
        protected LMTPServerModule lmtpServerModule() {
            return new TestLMTPServerModule();
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
        IMAPstore(IMAP_PORT).connect();
    }

    @Test (expected = AuthenticationFailedException.class)
    public void connectOnSecondaryIMAPServerIMAPServerShouldThrowWhenNoCredentials() throws Exception {
        IMAPstore(IMAP_PORT_SSL).connect();
    }

    @Test (expected = AuthenticationFailedException.class)
    public void connectPOP3ServerShouldThrowWhenNoCredentials() throws Exception {
        POP3store().connect();
    }

    @Test
    public void connectSMTPServerShouldNotThrowWhenNoCredentials() throws Exception {
        SMTPTransport().connect();
    }

    @Test(expected = MessagingException.class)
    public void connectLMTPServerShouldNotThrowWhenNoCredentials() throws Exception {
        LMTPTransport().connect();
    }

    private Store IMAPstore(int port) throws NoSuchProviderException {
        Properties properties = new Properties();
        properties.put("mail.imap.host", "localhost");
        properties.put("mail.imap.port", String.valueOf(port));
        Session session = Session.getDefaultInstance(properties);
        session.setDebug(true);
        return session.getStore("imap");
    }

    private Store POP3store() throws NoSuchProviderException {
        Properties properties = new Properties();
        properties.put("mail.pop3.host", "localhost");
        properties.put("mail.pop3.port", String.valueOf(POP3_PORT));
        Session session = Session.getDefaultInstance(properties);
        session.setDebug(true);
        return session.getStore("pop3");
    }

    private Transport SMTPTransport() throws NoSuchProviderException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        properties.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        Session session = Session.getDefaultInstance(properties);
        session.setDebug(true);
        return session.getTransport("smtp");
    }

    private Transport LMTPTransport() throws NoSuchProviderException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        properties.put("mail.smtp.port", String.valueOf(LMTP_PORT));
        Session session = Session.getDefaultInstance(properties);
        session.setDebug(true);
        return session.getTransport("smtp");
    }
}
