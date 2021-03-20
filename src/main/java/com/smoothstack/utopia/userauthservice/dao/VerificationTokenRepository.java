/**
 * 
 */
package com.smoothstack.utopia.userauthservice.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.VerificationToken;

/**
 * @author Craig Saunders
 *
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    public Optional<VerificationToken> findByToken(String token);
    public Optional<VerificationToken> findByUser(User user);
}
