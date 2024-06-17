package org.humancellatlas.ingest.security;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

@Repository
@RestResource(exported = false)
public interface AccountRepository extends MongoRepository<Account, String> {

  Account findByProviderReference(String providerReference);

  List<Account> findAccountByRoles(Role role);
}
