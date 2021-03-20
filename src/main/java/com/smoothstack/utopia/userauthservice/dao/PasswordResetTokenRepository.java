/**
 * 
 */
package com.smoothstack.utopia.userauthservice.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smoothstack.utopia.shared.model.PasswordResetToken;
import com.smoothstack.utopia.shared.model.User;

/**
 * @author Craig Saunders
 *
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    public Optional<PasswordResetToken> findByToken(String token);
    public Optional<PasswordResetToken> findByUser(User user);
    public boolean existsByToken(String token);
    public boolean existsByUser(User user);
}