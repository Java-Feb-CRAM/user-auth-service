/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smoothstack.utopia.shared.model.PasswordResetToken;
import com.smoothstack.utopia.shared.model.VerificationToken;
import com.smoothstack.utopia.userauthservice.dao.PasswordResetTokenRepository;
import com.smoothstack.utopia.userauthservice.dao.VerificationTokenRepository;

/**
 * @author Craig Saunders
 *
 */
@Service
@Transactional
public class UserSecurityService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    public enum TokenStatus {
        INVALID,
        VALID,
        EXPIRED
    }
    
    public TokenStatus validatePasswordResetToken(String token) {
        
        final PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).get();
        if (resetToken == null) {
            return TokenStatus.INVALID;
        }
        
        if ((resetToken.getExpiryDate().isBefore(LocalDateTime.now()))) {
            passwordResetTokenRepository.delete(resetToken);
            return TokenStatus.EXPIRED;
        }
        
        return TokenStatus.VALID;
    }
    
    public TokenStatus validateVerificationToken(String token)
    {
        return null;
    }
    
    private TokenStatus validate(String token, Class tokenClass, Object repositoryClass)
    {
        final VerificationToken verificationToken = verificationTokenRepository.findByToken(token).get();
        if (verificationToken == null) {
            return TokenStatus.INVALID;
        }
        
        if ((verificationToken.getExpiryDate().isBefore(LocalDateTime.now()))) {
            verificationTokenRepository.delete(verificationToken);
            return TokenStatus.EXPIRED;
        }
        
        return TokenStatus.VALID;
    }
}
