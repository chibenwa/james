/***********************************************************************
 * Copyright (c) 2006 The Apache Software Foundation.             *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/

package org.apache.james.transport.matchers;

import org.apache.james.test.mock.javaxmail.MockMimeMessage;
import org.apache.james.test.mock.mailet.MockMail;
import org.apache.james.test.mock.mailet.MockMailContext;
import org.apache.james.test.mock.mailet.MockMatcherConfig;

import org.apache.mailet.Matcher;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public class HasMailAttributeTest extends TestCase {

    private MimeMessage mockedMimeMessage;

    private MockMail mockedMail;

    private Matcher matcher;

    private final String MAIL_ATTRIBUTE_NAME = "org.apache.james.test.junit";

    private final String MAIL_ATTRIBUTE_VALUE = "true";

    private String mailAttributeName = "org.apache.james";

    private String mailAttributeValue = "false";

    public HasMailAttributeTest(String arg0)
            throws UnsupportedEncodingException {
        super(arg0);
    }

    private void setMailAttributeName(String mailAttributeName) {
        this.mailAttributeName = mailAttributeName;
    }

    private void setMailAttributeValue(String mailAttributeValue) {
        this.mailAttributeValue = mailAttributeValue;
    }

    private void setupMockedMimeMessage() throws MessagingException {
        String sender = "test@james.apache.org";
        String rcpt = "test2@james.apache.org";

        mockedMimeMessage = new MockMimeMessage();
        mockedMimeMessage.setFrom(new InternetAddress(sender));
        mockedMimeMessage.setRecipients(RecipientType.TO, rcpt);
        mockedMimeMessage.setSubject("testmail");
        mockedMimeMessage.setText("testtext");
        mockedMimeMessage.saveChanges();

    }

    private void setupMockedMail(MimeMessage m) {
        mockedMail = new MockMail();
        mockedMail.setMessage(m);
        mockedMail.setRecipients(Arrays.asList(new String[] {
                "test@james.apache.org", "test2@james.apache.org" }));
        mockedMail.setAttribute(mailAttributeName, (Serializable) mailAttributeValue);

    }

    private void setupMatcher() throws MessagingException {
        setupMockedMimeMessage();
        matcher = new HasMailAttribute();
        MockMatcherConfig mci = new MockMatcherConfig("HasMailAttribute="
                + MAIL_ATTRIBUTE_NAME, new MockMailContext());
        matcher.init(mci);
    }

    // test if the mail attribute was matched
    public void testHeaderIsMatched() throws MessagingException {
        setMailAttributeName(MAIL_ATTRIBUTE_NAME);
        setMailAttributeValue(MAIL_ATTRIBUTE_VALUE);

        setupMockedMimeMessage();
        setupMockedMail(mockedMimeMessage);
        setupMatcher();

        Collection matchedRecipients = matcher.match(mockedMail);

        assertNotNull(matchedRecipients);
        assertEquals(matchedRecipients.size(), mockedMail.getRecipients()
                .size());
    }

    // test if the mail attribute was not matched
    public void testHeaderIsNotMatched() throws MessagingException {
        setupMockedMimeMessage();
        setupMockedMail(mockedMimeMessage);
        setupMatcher();

        Collection matchedRecipients = matcher.match(mockedMail);

        assertNull(matchedRecipients);
    }
}
