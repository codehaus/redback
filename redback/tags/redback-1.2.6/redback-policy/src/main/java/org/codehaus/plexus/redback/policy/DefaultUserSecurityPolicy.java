package org.codehaus.plexus.redback.policy;

/*
 * Copyright 2001-2006 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.codehaus.plexus.redback.policy.rules.MustHavePasswordRule;
import org.codehaus.plexus.redback.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * User Security Policy.
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
@Service("userSecurityPolicy")
public class DefaultUserSecurityPolicy
    implements UserSecurityPolicy, Initializable, Contextualizable
{
    private static final String ENABLEMENT_KEY = ROLE + ":ENABLED";

    public static final String PASSWORD_RETENTION_COUNT = "security.policy.password.previous.count";

    public static final String LOGIN_ATTEMPT_COUNT = "security.policy.allowed.login.attempt";

    public static final String PASSWORD_EXPIRATION_ENABLED = "security.policy.password.expiration.enabled";
    
    public static final String PASSWORD_EXPIRATION = "security.policy.password.expiration.days";

    public static final String PASSWORD_ENCODER = "security.policy.password.encoder";

    public static final String UNLOCKABLE_ACCOUNTS = "security.policy.unlockable.accounts";

    private static final Logger log = LoggerFactory.getLogger( DefaultUserSecurityPolicy.class );

    private PasswordRule defaultPasswordRule = new MustHavePasswordRule();

    private PlexusContainer plexus;    
    
    @Resource (name="userConfiguration")
    private UserConfiguration config;

    @Resource(name="passwordEncoder#sha256")
    private PasswordEncoder passwordEncoder;

    @Resource(name="userValidationSettings")
    private UserValidationSettings userValidationSettings;

    @Resource(name="cookieSettings#rememberMe")
    private CookieSettings rememberMeCookieSettings;

    @Resource(name="cookieSettings#signon")
    private CookieSettings signonCookieSettings;    

    /**
     * The List of {@link PasswordRule} objects.
     */
    private List<PasswordRule> rules = new ArrayList<PasswordRule>();    
    
    private int previousPasswordsCount;

    private int loginAttemptCount;

    private int passwordExpirationDays;
    
    private boolean passwordExpirationEnabled;

    private List<String> unlockableAccounts;

    
    // ---------------------------------------
    //  Component lifecycle
    // ---------------------------------------
    @SuppressWarnings("unchecked")
    public void initialize()
        throws InitializationException
    {
        configurePolicy();

        configureEncoder();

        try
        {
            this.rules = plexus.lookupList( PasswordRule.ROLE );
        }
        catch ( ComponentLookupException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }
        // In some configurations, rules can be unset.
        if ( rules == null )
        {
            // Set rules to prevent downstream NPE.
            rules = new ArrayList<PasswordRule>();
        }

        if ( rules.isEmpty() )
        {
            // there should be at least one rule
            addPasswordRule( defaultPasswordRule );
        }
    }

    public void contextualize( Context context )
        throws ContextException
    {
        plexus = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    private void configureEncoder()
        throws InitializationException
    {
        String encoder = config.getString( PASSWORD_ENCODER );

        if ( encoder != null )
        {
            try
            {
                this.passwordEncoder = (PasswordEncoder) plexus.lookup( PasswordEncoder.ROLE, encoder );
            }
            catch ( ComponentLookupException e )
            {
                throw new InitializationException( "Unable to lookup password encoder.", e );
            }
        }
    }

    private void configurePolicy()
    {
        this.previousPasswordsCount = config.getInt( PASSWORD_RETENTION_COUNT );
        this.loginAttemptCount = config.getInt( LOGIN_ATTEMPT_COUNT );
        this.passwordExpirationEnabled = config.getBoolean( PASSWORD_EXPIRATION_ENABLED );
        this.passwordExpirationDays = config.getInt( PASSWORD_EXPIRATION );
        this.unlockableAccounts = config.getList( UNLOCKABLE_ACCOUNTS );
    }
    
    
    public String getId()
    {
        return "Default User Security Policy";
    }

    public int getPreviousPasswordsCount()
    {
        return previousPasswordsCount;
    }

    public List<String> getUnlockableAccounts()
    {
        if (unlockableAccounts == null)
        {
            unlockableAccounts = new ArrayList<String>();
        }
        return unlockableAccounts;
    }

    /**
     * Sets a list of accounts which should never be locked by security policy
     * @param unlockableAccounts
     */
    public void setUnlockableAccounts(List<String> unlockableAccounts)
    {
        this.unlockableAccounts = unlockableAccounts;
    }

    /**
     * Sets the count of previous passwords that should be tracked.
     *
     * @param count the count of previous passwords to track.
     */
    public void setPreviousPasswordsCount( int count )
    {
        this.previousPasswordsCount = count;
    }

    public int getLoginAttemptCount()
    {
        return loginAttemptCount;
    }

    public void setLoginAttemptCount( int count )
    {
        this.loginAttemptCount = count;
    }

    /**
     * Get the password encoder to be used for password operations
     *
     * @return the encoder
     */
    public PasswordEncoder getPasswordEncoder()
    {
        return passwordEncoder;
    }

    public boolean isEnabled()
    {
        Boolean bool = (Boolean) PolicyContext.getContext().get( ENABLEMENT_KEY );
        return bool == null || bool.booleanValue();
    }

    public void setEnabled( boolean enabled )
    {
        PolicyContext.getContext().put( ENABLEMENT_KEY, Boolean.valueOf( enabled ) );
    }

    /**
     * Add a Specific Rule to the Password Rules List.
     *
     * @param rule the rule to add.
     */
    public void addPasswordRule( PasswordRule rule )
    {
        // TODO: check for duplicates? if so, check should only be based on Rule class name.

        rule.setUserSecurityPolicy( this );
        this.rules.add( rule );
    }

    /**
     * Get the Password Rules List.
     *
     * @return the list of {@link PasswordRule} objects.
     */
    public List<PasswordRule> getPasswordRules()
    {
        return this.rules;
    }

    /**
     * Set the Password Rules List.
     *
     * @param rules the list of {@link PasswordRule} objects.
     */
    public void setPasswordRules( List<PasswordRule> rules )
    {
        this.rules.clear();

        if ( rules == null )
        {
            return;
        }

        // Intentionally iterating to ensure policy settings in provided rules.

        for (PasswordRule rule : rules)
        {
            addPasswordRule( rule );
        }
    }

    public void extensionPasswordExpiration( User user )
        throws MustChangePasswordException
    {
        if ( passwordExpirationEnabled && !getUnlockableAccounts().contains( user.getUsername() ) )
        {
            Calendar expirationDate = Calendar.getInstance();
            expirationDate.setTime( user.getLastPasswordChange() );
            expirationDate.add( Calendar.DAY_OF_MONTH, passwordExpirationDays );
            Calendar now = Calendar.getInstance();

            if ( now.after( expirationDate ) )
            {
                log.info( "User '" + user.getUsername() + "' flagged for password expiry (expired on: "
                    + expirationDate + ")" );
                user.setPasswordChangeRequired( true );
                throw new MustChangePasswordException( "Password Expired, You must change your password.", user );
            }
        }
    }

    public void extensionExcessiveLoginAttempts( User user )
        throws AccountLockedException
    {
        if ( !getUnlockableAccounts().contains( user.getUsername() ) )
        {
            int attempt = user.getCountFailedLoginAttempts();
            attempt++;
            user.setCountFailedLoginAttempts( attempt );

            if ( attempt >= loginAttemptCount )
            {
                log.info( "User '" + user.getUsername() + "' locked due to excessive login attempts: " + attempt );
                user.setLocked( true );
                throw new AccountLockedException( "Account " + user.getUsername() + " is locked.", user );
            }
        }
    }

    public void extensionChangePassword( User user )
        throws PasswordRuleViolationException
    {
        validatePassword( user );

        // set the current encoded password.
        user.setEncodedPassword( passwordEncoder.encodePassword( user.getPassword() ) );
        user.setPassword( null );

        // push new password onto list of previous password.
        List<String> previousPasswords = new ArrayList<String>();
        previousPasswords.add( user.getEncodedPassword() );

        if ( !user.getPreviousEncodedPasswords().isEmpty() )
        {
            int oldCount = Math.min( previousPasswordsCount - 1, user.getPreviousEncodedPasswords().size() );
            //modified sublist start index as the previous value results to nothing being added to the list. 
            List<String> sublist = user.getPreviousEncodedPasswords().subList( 0, oldCount );
            previousPasswords.addAll( sublist );
        }

        user.setPreviousEncodedPasswords( previousPasswords );
        user.setPasswordChangeRequired( false );

        // Update timestamp for password change.
        user.setLastPasswordChange( new Date() );
    }

    public void validatePassword( User user )
        throws PasswordRuleViolationException
    {
        if ( isEnabled() )
        {
            PasswordRuleViolations violations = new PasswordRuleViolations();

            for (PasswordRule rule : this.rules)
            {
                if ( rule.isEnabled() )
                {
                    if ( rule.requiresSecurityPolicy() )
                    {
                        rule.setUserSecurityPolicy( this );
                    }

                    rule.testPassword( violations, user );
                }
            }

            if ( violations.hasViolations() )
            {
                PasswordRuleViolationException exception = new PasswordRuleViolationException();
                exception.setViolations( violations );
                throw exception;
            }
        }

        // If you got this far, then ensure that the password is never null.
        if ( user.getPassword() == null )
        {
            user.setPassword( "" );
        }
    }
    
    public int getPasswordExpirationDays()
    {
        return passwordExpirationDays;
    }

    public void setPasswordExpirationDays( int passwordExpiry )
    {
        this.passwordExpirationDays = passwordExpiry;
    }

    public UserValidationSettings getUserValidationSettings()
    {
        return userValidationSettings;
    }

    public void setUserValidationSettings( UserValidationSettings settings )
    {
        this.userValidationSettings = settings;
    }

    public CookieSettings getRememberMeCookieSettings()
    {
        return rememberMeCookieSettings;
    }

    public CookieSettings getSignonCookieSettings()
    {
        return signonCookieSettings;
    }

}
