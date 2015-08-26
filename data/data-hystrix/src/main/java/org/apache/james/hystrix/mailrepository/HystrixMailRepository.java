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

import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.james.hystrix.mailrepository.commands.*;
import org.apache.james.mailrepository.api.MailRepository;
import org.apache.mailet.Mail;

import javax.mail.MessagingException;
import java.util.Collection;
import java.util.Iterator;

public class HystrixMailRepository implements MailRepository {

    private final MailRepository wrappedMailRepository;

    public HystrixMailRepository(MailRepository wrappedMailRepository) {
        this.wrappedMailRepository = wrappedMailRepository;
    }

    @Override
    public void store(Mail mc) throws MessagingException {
        try {
            new StoreCommand(wrappedMailRepository, mc).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public Iterator<String> list() throws MessagingException {
        try {
            return new ListMailCommand(wrappedMailRepository).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public Mail retrieve(String key) throws MessagingException {
        try {
            return new RetrieveMailCommand(wrappedMailRepository, key).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void remove(Mail mail) throws MessagingException {
        try {
            new RemoveMailCommand(wrappedMailRepository, mail).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void remove(Collection<Mail> mails) throws MessagingException {
        try {
            new RemoveMailsCommand(wrappedMailRepository, mails).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void remove(String key) throws MessagingException {
        try {
            new RemoveMailKeyCommand(wrappedMailRepository, key).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public boolean lock(String key) throws MessagingException {
        try {
            return new LockCommand(wrappedMailRepository, key).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public boolean unlock(String key) throws MessagingException {
        try {
            return new UnlockCommand(wrappedMailRepository, key).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    private MessagingException unboxException(HystrixBadRequestException e) {
        if (e.getCause() instanceof MessagingException) {
            return  (MessagingException) e.getCause();
        }
        throw new RuntimeException("HystrixBadRequestException wasn't containing MailboxException", e);
    }
}
