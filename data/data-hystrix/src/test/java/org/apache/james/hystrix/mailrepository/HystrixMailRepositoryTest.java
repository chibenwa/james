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

package org.apache.james.hystrix.mailrepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.james.mailrepository.api.MailRepository;
import org.apache.mailet.Mail;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;

public class HystrixMailRepositoryTest {

    private static final String KEY = "key";

    private MailRepository mockedMailRepository;
    private MailRepository testee;

    @Before
    public void setUp() {
        mockedMailRepository = mock(MailRepository.class);
        testee = new HystrixMailRepository(mockedMailRepository);
    }

    @Test
    public void listShouldCallUnderlyingMailRepository() throws Exception {
        testee.list();
        verify(mockedMailRepository).list();
    }

    @Test
    public void lockShouldCallUnderlyingMailRepository() throws Exception {
        testee.lock(KEY);
        verify(mockedMailRepository).lock(KEY);
    }

    @Test
    public void removeShouldCallUnderlyingMailRepository() throws Exception {
        testee.remove((Mail) null);
        verify(mockedMailRepository).remove((Mail) null);
    }

    @Test
    public void removeStringShouldCallUnderlyingMailRepository() throws Exception {
        testee.remove(KEY);
        verify(mockedMailRepository).remove(KEY);
    }

    @Test
    public void removeListShouldCallUnderlyingMailRepository() throws Exception {
        List<Mail> list = new ArrayList<>();
        testee.remove(list);
        verify(mockedMailRepository).remove(list);
    }

    @Test
    public void retrieveShouldCallUnderlyingMailRepository() throws Exception {
        testee.retrieve(KEY);
        verify(mockedMailRepository).retrieve(KEY);
    }

    @Test
    public void storeShouldCallUnderlyingMailRepository() throws Exception {
        testee.store(null);
        verify(mockedMailRepository).store(null);
    }

    @Test
    public void unlockShouldCallUnderlyingMailRepository() throws Exception {
        testee.unlock(KEY);
        verify(mockedMailRepository).unlock(KEY);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = MessagingException.class)
    public void messagingExceptionShouldBePropagated() throws Exception {
        when(mockedMailRepository.retrieve(KEY)).thenThrow(MessagingException.class);
        testee.retrieve(KEY);
    }

}
