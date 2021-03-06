package org.codehaus.plexus.redback.struts2.action.admin;

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

import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;
import org.codehaus.redback.integration.model.EditUserCredentials;

/**
 * AddAdminUserAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id: AddAdminUserAction.java 448077 2006-09-20 05:42:22Z joakime $
 * @plexus.component role="com.opensymphony.xwork2.Action"
 * role-hint="redback-admin-account"
 * instantiation-strategy="per-lookup"
 */
public class AddAdminUserAction
    extends AbstractAdminUserCredentialsAction
{
    /**
     * @plexus.requirement
     */
    private RoleManager roleManager;


    /**
     * @plexus.requirement role-hint="default"
     */
    private UserConfiguration config;

    private EditUserCredentials user;

    public String show()
    {
        if ( user == null )
        {
            user = new EditUserCredentials( config.getString( "redback.default.admin" ) );
        }

        return INPUT;
    }

    public String submit()
    {
        if ( user == null )
        {
            user = new EditUserCredentials( config.getString( "redback.default.admin" ) );
            addActionError( getText( "invalid.admin.credentials" ) );
            return ERROR;
        }

        getLogger().info( "user = " + user );

        internalUser = user;

        validateCredentialsStrict();

        UserManager userManager = super.securitySystem.getUserManager();

        if ( userManager.userExists( config.getString( "redback.default.admin" ) ) )
        {
            // Means that the role name exist already.
            // We need to fail fast and return to the previous page.
            addActionError( getText( "admin.user.already.exists" ) );
            return ERROR;
        }

        if ( hasActionErrors() || hasFieldErrors() )
        {
            return ERROR;
        }

        User u =
            userManager.createUser( config.getString( "redback.default.admin" ), user.getFullName(), user.getEmail() );
        if ( u == null )
        {
            addActionError( getText( "cannot.operate.on.null.user" ) );
            return ERROR;
        }

        u.setPassword( user.getPassword() );
        u.setLocked( false );
        u.setPasswordChangeRequired( false );
        u.setPermanent( true );

        userManager.addUser( u );

        try
        {
            roleManager.assignRole( "system-administrator", u.getPrincipal().toString() );
        }
        catch ( RoleManagerException rpe )
        {
            addActionError( getText( "cannot.assign.admin.role" ) );
            return ERROR;
        }

        return "security-admin-user-created";
    }

    public EditUserCredentials getUser()
    {
        return user;
    }

    public void setUser( EditUserCredentials user )
    {
        this.user = user;
    }

    public SecureActionBundle initSecureActionBundle()
        throws SecureActionException
    {
        return SecureActionBundle.OPEN;
    }
}
