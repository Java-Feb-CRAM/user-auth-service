/**
 * 
 */
package com.smoothstack.utopia.userauthservice.dao;

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
    public VerificationToken findByToken(String token);
    public VerificationToken findByUser(User user);
    public boolean existsByToken(String token);
    public boolean existsByUser(User user);
}
