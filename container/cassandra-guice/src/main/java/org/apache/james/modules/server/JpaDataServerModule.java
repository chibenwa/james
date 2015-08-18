

package org.apache.james.modules.server;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.domainlist.jpa.JPADomainList;
import org.apache.james.rrt.api.RecipientRewriteTable;
import org.apache.james.rrt.jpa.JPARecipientRewriteTable;
import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.jpa.JPAUsersRepository;

public class JpaDataServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RecipientRewriteTable.class).toInstance(new JPARecipientRewriteTable());
        createDomainList();
        createUserRepository();
    }

    private void createDomainList() {
        JPADomainList jpaDomainList = new JPADomainList();
        bind(DomainList.class).toInstance(jpaDomainList);
        bind(DomainList.class).annotatedWith(Names.named("domainlist")).toInstance(jpaDomainList);
    }

    private void createUserRepository() {
        JPAUsersRepository jpaUsersRepository = new JPAUsersRepository();
        bind(UsersRepository.class).toInstance(jpaUsersRepository);
        bind(UsersRepository.class).annotatedWith(Names.named("usersrepository")).toInstance(jpaUsersRepository);
    }
}
