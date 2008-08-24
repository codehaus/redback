package org.codehaus.plexus.redback.xwork.action.admin;

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

import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.redback.xwork.action.AbstractSecurityAction;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionBundle;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionException;
import org.codehaus.plexus.redback.xwork.role.RoleConstants;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * EditRoleAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action"
 * role-hint="redback-role-edit"
 * instantiation-strategy="per-lookup"
 */
public class EditRoleAction
    extends AbstractSecurityAction
{
    // ------------------------------------------------------------------
    // Plexus Component Requirements
    // ------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private SecuritySystem securitySystem;

    /**
     * @plexus.requirement role-hint="cached"
     */
    private RBACManager manager;

    // ------------------------------------------------------------------
    // Action Parameters
    // ------------------------------------------------------------------

    private String name;

    private String description;

    private List childRoleNames = new ArrayList();

    private List permissions = new ArrayList();

    private List users = new ArrayList();

    private List allUsers = new ArrayList();

    // ------------------------------------------------------------------
    // Action Entry Points - (aka Names)
    // ------------------------------------------------------------------

    public String input()
    {
        if ( name == null )
        {
            addActionError( getText( "cannot.edit.null.role" ) );
            return ERROR;
        }

        if ( StringUtils.isEmpty( name ) )
        {
            addActionError( getText( "cannot.edit.empty.role" ) );
            return ERROR;
        }

        if ( !manager.roleExists( name ) )
        {
            // Means that the role name doesn't exist.
            // We should exit early and not attempt to look up the role information.
            return INPUT;
        }

        try
        {
            Role role = manager.getRole( name );
            if ( role == null )
            {
                addActionError( getText( "cannot.operate.null.role" ) );
                return ERROR;
            }

            description = role.getDescription();
            childRoleNames = role.getChildRoleNames();
            permissions = role.getPermissions();
            List roles = new ArrayList();
            roles.add( name );
            List userAssignments = manager.getUserAssignmentsForRoles( roles );
            if ( userAssignments != null )
            {
                for ( Iterator i = userAssignments.iterator(); i.hasNext(); )
                {
                    UserAssignment userAssignment = (UserAssignment) i.next();
                    try
                    {
                        User user = getUserManager().findUser( userAssignment.getPrincipal() );
                        users.add( user );
                    }
                    catch ( UserNotFoundException e )
                    {
                        getLogger().warn( "User '" + userAssignment.getPrincipal() + "' doesn't exist.", e );
                    }
                }
            }
        }
        catch ( RbacManagerException e )
        {
            List list = new ArrayList();
            list.add( name );
            list.add( e.getMessage() );
            addActionError( getText( "cannot.get.role", list ) );
            return ERROR;
        }

        return INPUT;
    }

    public String edit()
    {
        String result = input();

        //TODO: Remove all users defined in parent roles too
        allUsers = getUserManager().getUsers();
        getLogger().info( "edit:" + allUsers.size() );
        for ( Iterator i = users.iterator(); i.hasNext(); )
        {
            User user = (User) i.next();
            if ( allUsers.contains( user ) )
            {
                allUsers.remove( user );
            }
        }

        return result;
    }

    public String submit()
    {
        if ( name == null )
        {
            addActionError( getText( "cannot.edit.null.role" ) );
            return ERROR;
        }

        if ( StringUtils.isEmpty( name ) )
        {
            addActionError( getText( "cannot.edit.empty.role" ) );
            return ERROR;
        }

        try
        {
            Role role;
            if ( manager.roleExists( name ) )
            {
                role = manager.getRole( name );
            }
            else
            {
                role = manager.createRole( name );
            }

            role.setDescription( description );
            //role.setChildRoleNames( childRoleNames );
            //role.setPermissions( permissions );

            manager.saveRole( role );

            List list = new ArrayList();
            list.add( name );
            addActionMessage( getText( "save.role.success", list ) );
        }
        catch ( RbacManagerException e )
        {
            List list = new ArrayList();
            list.add( name );
            list.add( e.getMessage() );
            addActionError( getText( "cannot.get.role", list ) );
            return ERROR;
        }

        return SUCCESS;
    }

    private UserManager getUserManager()
    {
        return securitySystem.getUserManager();
    }

    // ------------------------------------------------------------------
    // Parameter Accessor Methods
    // ------------------------------------------------------------------

    public String getName()
    {
        return name;
    }

    public void setName( String roleName )
    {
        this.name = roleName;
    }

    public List getChildRoleNames()
    {
        return childRoleNames;
    }

    public void setChildRoleNames( List childRoleNames )
    {
        this.childRoleNames = childRoleNames;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public List getPermissions()
    {
        return permissions;
    }

    public void setPermissions( List permissions )
    {
        this.permissions = permissions;
    }

    public List getUsers()
    {
        return users;
    }

    public void setUsers( List users )
    {
        this.users = users;
    }

    public List getAllUsers()
    {
        return allUsers;
    }

    public void setAllUsers( List allUsers )
    {
        this.allUsers = allUsers;
    }
// ------------------------------------------------------------------
    // Internal Support Methods
    // ------------------------------------------------------------------

    public SecureActionBundle initSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_RBAC_ADMIN_OPERATION, Resource.GLOBAL );
        return bundle;
    }

}
