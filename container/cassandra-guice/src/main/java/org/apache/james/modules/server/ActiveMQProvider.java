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

import com.google.inject.Singleton;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.blob.BlobTransferPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.james.queue.activemq.ActiveMQMailQueueFactory;
import org.apache.james.queue.activemq.FileSystemBlobTransferPolicy;
import org.apache.james.queue.jms.JMSMailQueueFactory;

import javax.inject.Provider;

@Singleton
public class ActiveMQProvider implements Provider<JMSMailQueueFactory> {

    @Override
    public JMSMailQueueFactory get() {
        launchEmbeddedBroker();
        return createActiveMailQueueFactory(
            createActiveMQConnectionFactory(
                createBlobTransferPolicy()));
    }

    private JMSMailQueueFactory createActiveMailQueueFactory(ActiveMQConnectionFactory connectionFactory) {
        ActiveMQMailQueueFactory result = new ActiveMQMailQueueFactory();
        result.setConnectionFactory(connectionFactory);
        return result;
    }

    private ActiveMQConnectionFactory createActiveMQConnectionFactory(BlobTransferPolicy blobTransferPolicy) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://james?create=false");
        connectionFactory.setBlobTransferPolicy(blobTransferPolicy);
        return connectionFactory;
    }

    private BlobTransferPolicy createBlobTransferPolicy() {
        BlobTransferPolicy blobTransferPolicy = new FileSystemBlobTransferPolicy();
        blobTransferPolicy.setDefaultUploadUrl("file://var/store/activemq/blob-transfer");
        return blobTransferPolicy;
    }

    private void launchEmbeddedBroker() {
        BrokerService brokerService = new BrokerService();
        brokerService.setBrokerName("james");
        brokerService.setUseJmx(true);
        brokerService.setPersistent(true);
        brokerService.setDataDirectory("filesystem=file://var/store/activemq/brokers\"");
        brokerService.setUseShutdownHook(false);
        brokerService.setSchedulerSupport(false);
        brokerService.setBrokerId("broker");
        String[] uris = {"tcp://localhost:0"};
        brokerService.setTransportConnectorURIs(uris);
    }
}
