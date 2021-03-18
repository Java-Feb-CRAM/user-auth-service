/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

/**
 * @author Craig Saunders
 *
 */
public interface ISecurityUserService {

    String validatePasswordResetToken(String token);

}