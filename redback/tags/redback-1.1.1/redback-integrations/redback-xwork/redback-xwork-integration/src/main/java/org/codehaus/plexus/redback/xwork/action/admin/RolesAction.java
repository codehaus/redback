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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.xwork.action.AbstractSecurityAction;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionBundle;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionException;
import org.codehaus.plexus.redback.xwork.role.RoleConstants;
import org.codehaus.plexus.redback.xwork.util.RoleSorter;

/**
 * RolesAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action"
 * role-hint="redback-roles"
 * instantiation-strategy="per-lookup"
 */
public class RolesAction
    extends AbstractSecurityAction
{
    private static final String LIST = "list";

    /**
     * @plexus.requirement role-hint="cached"
     */
    private RBACManager manager;

    private List allRoles;

    public String list()
    {
        try
        {
            allRoles = manager.getAllRoles();

            if ( allRoles == null )
            {
                allRoles = Collections.EMPTY_LIST;
            }

            Collections.sort( allRoles, new RoleSorter() );
        }
        catch ( RbacManagerException e )
        {
            List list = new ArrayList();
            list.add( e.getMessage() );
            addActionError( getText( "cannot.list.all.roles", list ) );
            getLogger().error( "System error:", e );
            allRoles = Collections.EMPTY_LIST;
        }

        return LIST;
    }

    public List getAllRoles()
    {
        return allRoles;
    }

    public void setAllRoles( List allRoles )
    {
        this.allRoles = allRoles;
    }

    public SecureActionBundle initSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_RBAC_ADMIN_OPERATION, Resource.GLOBAL );
        return bundle;
    }
}
