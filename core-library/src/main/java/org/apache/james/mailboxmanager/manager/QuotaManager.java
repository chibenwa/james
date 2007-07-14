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

package org.apache.james.mailboxmanager.manager;

import org.apache.james.mailboxmanager.ListResult;
import org.apache.james.mailboxmanager.Quota;
import org.apache.james.services.User;

/**
 * manages quota. Only getQuotas() will be used by MailboxRepository to create a
 * session. The other methods will be used by a management user interface
 * (console/webapp/jmx...) or IMAP ACL extensions.
 * <p>
 * The only operation that requires dealing with the named repositories directly
 * is the quota management. It is probably really difficult to implement a quota
 * system that spans multiple repository implementations. That is why quotas are
 * created for a specific repository. To be able to administer, repositories and
 * theier belonging mailboxes can be listet.
 * </p>
 * 
 */

public interface QuotaManager {

    /**
     * Get the quotas that are associated to this mailbox
     * 
     * @param requestingUser
     *            to check credentials
     */

    Quota[] getQuotas(String mailboxName, User requestingUser);

    /**
     * list the available repositories
     * 
     * @return names of the repositories
     */
    String[] listRepositories();

    /**
     * List the mailboxes that exist in this repository.
     * 
     * @param repositoryName
     * @param base
     * @param expression
     * @param user
     */

    ListResult[] listForRepository(String repositoryName, String base,
            String expression, User user);

    /**
     * Lists quotas the user is allowed to manage. Via base and expression the
     * user is able to browse quotas that are bound to a root mailbox.
     * 
     * @param repositoryName
     * @param base
     *            like in list()
     * @param expression
     *            like in list(), if empty only namespace-root quotas will be
     *            listet
     * @param requestingUser
     */
    Quota[] listQuotas(String repositoryName, String base, String expression,
            User requestingUser);

    /**
     * <p>
     * create a new quota in the given repository
     * </p>
     * <p>
     * An example sitatuation where users create quotas could be setting up auto
     * purge for their Trash mailbox. User may be limited to the store where
     * theier mailbox resists. To avoid chaos a naming schema should be used.
     * </p>
     * Changing/updating quotas will be done in quota object itself by calling
     * save() method
     * 
     * @param repositoryName
     * @param rootMailbox
     *            the root mailbox the quota will be bound to, maybe an empty
     *            String for a namespace root quota
     * @param quotaName
     *            free chosen, has to be unique for the root mailbox
     * @param user
     *            to check credentials
     * @return a new quota object that can be populated with values
     */

    Quota createQuota(String repositoryName, String rootMailbox,
            String quotaName, User user);

    Quota getQuota(String repositoryName, String rootMailbox, String quotaName,
            User user);

    void removeQuota(String repositoryName, String quotaName,
            User requestingUser);

}
