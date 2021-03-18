/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

import java.util.Calendar;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smoothstack.utopia.shared.model.PasswordResetToken;
import com.smoothstack.utopia.userauthservice.dao.PasswordResetTokenRepository;

/**
 * @author Craig Saunders
 *
 */
@Service
@Transactional
public class UserSecurityService implements ISecurityUserService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public String validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token);

        return !isTokenFound(passToken) ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : null;
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }
}
