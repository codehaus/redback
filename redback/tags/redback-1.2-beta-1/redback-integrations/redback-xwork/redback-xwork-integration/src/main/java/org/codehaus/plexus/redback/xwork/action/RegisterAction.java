package org.codehaus.plexus.redback.xwork.action;

/*
 * Copyright 2005-2006 The Codehaus.
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
import java.util.List;

import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManagerException;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;
import org.codehaus.redback.integration.mail.Mailer;
import org.codehaus.redback.integration.model.CreateUserCredentials;

/**
 * RegisterAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action"
 * role-hint="redback-register"
 * instantiation-strategy="per-lookup"
 */
public class RegisterAction
    extends AbstractUserCredentialsAction
    implements CancellableAction
{
    protected static final String REGISTER_SUCCESS = "security-register-success";

    private static final String VALIDATION_NOTE = "validation-note";

    private static final String RESEND_VALIDATION_EMAIL = "security-resend-validation-email";
    
    // ------------------------------------------------------------------
    // Plexus Component Requirements
    // ------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private Mailer mailer;

    /**
     * @plexus.requirement role-hint="cached"
     */
    private RBACManager rbacManager;

    /**
     * @plexus.requirement
     */
    private RoleManager roleManager;

    private CreateUserCredentials user;

    private boolean emailValidationRequired;

    private String username;
    
    // ------------------------------------------------------------------
    // Action Entry Points - (aka Names)
    // ------------------------------------------------------------------

    public String show()
    {
        if ( user == null )
        {
            user = new CreateUserCredentials();
        }

        emailValidationRequired = securitySystem.getPolicy().getUserValidationSettings().isEmailValidationRequired();

        return INPUT;
    }

    public String register()
    {
        if ( user == null )
        {
            user = new CreateUserCredentials();
            addActionError( getText( "invalid.user.credentials" ) );
            return ERROR;
        }

        UserSecurityPolicy securityPolicy = securitySystem.getPolicy();

        emailValidationRequired = securityPolicy.getUserValidationSettings().isEmailValidationRequired();

        internalUser = user;

        if ( securityPolicy.getUserValidationSettings().isEmailValidationRequired() )
        {
            validateCredentialsLoose();
        }
        else
        {
            validateCredentialsStrict();
        }

        // NOTE: Do not perform Password Rules Validation Here.
        UserManager manager = super.securitySystem.getUserManager();

        if ( manager.userExists( user.getUsername() ) )
        {
            // Means that the role name doesn't exist.
            // We need to fail fast and return to the previous page.
            List list = new ArrayList();
            list.add( user.getUsername() );
            addActionError( getText( "user.already.exists", list ) );
        }

        if ( hasActionErrors() || hasFieldErrors() )
        {
            return ERROR;
        }

        User u = manager.createUser( user.getUsername(), user.getFullName(), user.getEmail() );
        u.setPassword( user.getPassword() );
        u.setValidated( false );
        u.setLocked( false );

        try
        {
            roleManager.assignRole( "registered-user", u.getPrincipal().toString() );
        }
        catch ( RoleManagerException rpe )
        {
            addActionError( getText( "assign.role.failure" ) );
            getLogger().error( "RoleProfile Error: " + rpe.getMessage(), rpe );
            return ERROR;
        }

        if ( securityPolicy.getUserValidationSettings().isEmailValidationRequired() )
        {
            u.setLocked( true );

            try
            {
                AuthenticationKey authkey = securitySystem.getKeyManager().createKey( u.getPrincipal().toString(),
                                                                                      "New User Email Validation",
                                                                                      securityPolicy.getUserValidationSettings().getEmailValidationTimeout() );

                List recipients = new ArrayList();
                recipients.add( u.getEmail() );

                mailer.sendAccountValidationEmail( recipients, authkey, getBaseUrl() );

                securityPolicy.setEnabled( false );
                manager.addUser( u );

                return VALIDATION_NOTE;
            }
            catch ( KeyManagerException e )
            {
                addActionError( getText( "cannot.register.user" ) );
                getLogger().error( "Unable to register a new user.", e );
                return ERROR;
            }
            finally
            {
                securityPolicy.setEnabled( true );
            }
        }
        else
        {
            manager.addUser( u );
        }

        return REGISTER_SUCCESS;
    }

    public String resendRegistrationEmail()
    {
    	UserSecurityPolicy securityPolicy = securitySystem.getPolicy();
    	   	
        try
        {
        	User user = super.securitySystem.getUserManager().findUser( username );        	
        	
            AuthenticationKey authkey = securitySystem.getKeyManager().createKey( user.getPrincipal().toString(),
                                                                                  "New User Email Validation",
                                                                                  securityPolicy.getUserValidationSettings().getEmailValidationTimeout() );
            
            List recipients = new ArrayList();
            recipients.add( user.getEmail() );

            mailer.sendAccountValidationEmail( recipients, authkey, getBaseUrl() );

            return RESEND_VALIDATION_EMAIL;
        }
        catch ( KeyManagerException e )
        {
            addActionError( getText( "cannot.register.user" ) );
            getLogger().error( "Unable to register a new user.", e );
            return ERROR;
        } 
        catch ( UserNotFoundException e ) 
        {
			addActionError( getText( "cannot.find.user" ) );
            getLogger().error( "Unable to find user.", e );
            return ERROR;
		} 	
    }
    
    public String cancel()
    {
        return CANCEL;
    }

    // ------------------------------------------------------------------
    // Parameter Accessor Methods
    // ------------------------------------------------------------------

    public CreateUserCredentials getUser()
    {
        return user;
    }

    public void setUser( CreateUserCredentials user )
    {
        this.user = user;
    }

    public boolean isEmailValidationRequired()
    {
        return emailValidationRequired;
    }

    public void setEmailValidationRequired( boolean emailValidationRequired )
    {
        this.emailValidationRequired = emailValidationRequired;
    }
    
    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public SecureActionBundle initSecureActionBundle()
        throws SecureActionException
    {
        return SecureActionBundle.OPEN;
    }
}
