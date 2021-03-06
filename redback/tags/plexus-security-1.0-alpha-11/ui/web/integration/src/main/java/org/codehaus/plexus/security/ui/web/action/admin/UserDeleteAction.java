package org.codehaus.plexus.security.ui.web.action.admin;

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

import org.codehaus.plexus.security.rbac.RBACManager;
import org.codehaus.plexus.security.rbac.RbacManagerException;
import org.codehaus.plexus.security.rbac.RbacObjectInvalidException;
import org.codehaus.plexus.security.rbac.RbacObjectNotFoundException;
import org.codehaus.plexus.security.rbac.Resource;
import org.codehaus.plexus.security.ui.web.action.AbstractSecurityAction;
import org.codehaus.plexus.security.ui.web.action.CancellableAction;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;
import org.codehaus.plexus.security.ui.web.role.profile.RoleConstants;
import org.codehaus.plexus.security.user.UserManager;
import org.codehaus.plexus.security.user.UserNotFoundException;
import org.codehaus.plexus.util.StringUtils;

/**
 * UserDeleteAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action"
 * role-hint="pss-admin-user-delete"
 * instantiation-strategy="per-lookup"
 */
public class UserDeleteAction
    extends AbstractSecurityAction
    implements CancellableAction
{
    // ------------------------------------------------------------------
    // Plexus Component Requirements
    // ------------------------------------------------------------------

    /**
     * @plexus.requirement role-hint="cached"
     */
    private UserManager userManager;

    /**
     * @plexus.requirement role-hint="cached"
     */
    private RBACManager rbacManager;

    // ------------------------------------------------------------------
    // Action Parameters
    // ------------------------------------------------------------------

    private String username;

    // ------------------------------------------------------------------
    // Action Entry Points - (aka Names)
    // ------------------------------------------------------------------

    public String confirm()
    {
        if ( username == null )
        {
            addActionError( "Unable to delete user based on null username." );
            return ERROR;
        }

        return INPUT;
    }

    public String submit()
    {
        if ( username == null )
        {
            addActionError( "Invalid user credentials." );
            return ERROR;
        }

        if ( StringUtils.isEmpty( username ) )
        {
            addActionError( "Unable to delete user based on empty username." );
            return ERROR;
        }

        try
        {
        	rbacManager.removeUserAssignment( username );
            userManager.deleteUser( username );            
        }
        catch ( UserNotFoundException e )
        {
            addActionError( "Unable to delete non-existant user '" + username + "'" );
        }
        catch ( RbacObjectNotFoundException e )
        {        	
            // ignore, this is possible since the user may never have had roles assigned
        }
        catch ( RbacObjectInvalidException e )
        {
            addActionError( "unable to remove user role assignments for '" + username + "' because " + e.getMessage() );
        }
        catch ( RbacManagerException e )
        {
            addActionError( "unable to remove user role assignments for '" + username + "' because " + e.getMessage() );
        }

        return SUCCESS;
    }

    /**
     * Returns the cancel result. <p/> A basic implementation would simply be to return CANCEL.
     * 
     * @return
     */
    public String cancel()
    {
        return CANCEL;
    }

    // ------------------------------------------------------------------
    // Parameter Accessor Methods
    // ------------------------------------------------------------------

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public SecureActionBundle initSecureActionBundle() throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_USER_DELETE_OPERATION, Resource.GLOBAL );
        return bundle;
    }

}
