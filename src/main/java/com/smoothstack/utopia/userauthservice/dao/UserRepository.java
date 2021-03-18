/**
 * 
 */
package com.smoothstack.utopia.userauthservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smoothstack.utopia.shared.model.User;


/**
 * @author Craig Saunders
 *
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {    
    public User findByUsername(String username);
    public User findByEmail(String email);
    public boolean existsByUsername(String username);
    public boolean existsByEmail(String email);
}
