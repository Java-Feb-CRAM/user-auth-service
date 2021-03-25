/**
 * 
 */
package com.smoothstack.utopia.userauthservice.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smoothstack.utopia.shared.model.User;

/**
 * @author Craig Saunders
 *
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByUsername(String username);
    public List<User> findByUsernameIgnoreCase(String username);
    public Optional<User> findByEmail(String email);
}
