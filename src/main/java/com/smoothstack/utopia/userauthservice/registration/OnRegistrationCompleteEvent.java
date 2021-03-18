/**
 * 
 */
package com.smoothstack.utopia.userauthservice.registration;

import org.springframework.context.ApplicationEvent;

import com.smoothstack.utopia.shared.model.User;

import lombok.Getter;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 1317799221067823116L;
    private final String appUrl;
    private final User user;

    public OnRegistrationCompleteEvent(final User user, final String appUrl) {
        super(user);
        this.user = user;
        this.appUrl = appUrl;
    }
}