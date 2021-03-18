/**
 * 
 */
package com.smoothstack.utopia.userauthservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smoothstack.utopia.shared.model.UserRole;

/**
 * @author Craig Saunders
 *
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    public UserRole findById(int id);
    public UserRole findByName(String name);
    public boolean existsById(int id);
    public boolean existsByName(String name);
}
