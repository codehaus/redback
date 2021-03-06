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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.struts2.action.AbstractSecurityAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;
import org.codehaus.redback.integration.role.RoleConstants;
import org.codehaus.redback.integration.util.ResourceSorter;

/**
 * OperationsAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action"
 * role-hint="redback-resources"
 * instantiation-strategy="per-lookup"
 */
public class ResourcesAction
    extends AbstractSecurityAction
{
    private static final String LIST = "list";

    /**
     * @plexus.requirement role-hint="cached"
     */
    private RBACManager manager;

    private String resourceIdentifier;

    private boolean isPattern;

    private List allResources;

    public String list()
    {
        try
        {
            allResources = manager.getAllResources();

            if ( allResources == null )
            {
                allResources = Collections.EMPTY_LIST;
            }

            Collections.sort( allResources, new ResourceSorter() );
        }
        catch ( RbacManagerException e )
        {
            List list = new ArrayList();
            list.add( e.getMessage() );
            addActionError( getText( "cannot.list.all.resources", list ) );
            getLogger().error( "System error:", e );
            allResources = Collections.EMPTY_LIST;
        }

        return LIST;
    }

    public String save()
    {
        try
        {
            Resource temp = manager.createResource( resourceIdentifier );

            temp.setIdentifier( resourceIdentifier );
            temp.setPattern( isPattern );

            manager.saveResource( temp );
        }
        catch ( RbacManagerException e )
        {
            List list = new ArrayList();
            list.add( e.getMessage() );
            addActionError( getText( "cannot.save.resource", list ) );
            getLogger().error( "System error:", e );
            allResources = Collections.EMPTY_LIST;
        }

        return LIST;
    }

    public String remove()
    {
        try
        {
            manager.removeResource( manager.getResource( resourceIdentifier ) );
        }
        catch ( RbacManagerException ne )
        {
            List list = new ArrayList();
            list.add( resourceIdentifier );
            addActionError( getText( "cannot.remove.resource", list ) );
            return ERROR;
        }
        return LIST;
    }

    public List getAllResources()
    {
        return allResources;
    }

    public void setAllResources( List allResources )
    {
        this.allResources = allResources;
    }

    public String getResourceIdentifier()
    {
        return resourceIdentifier;
    }

    public void setResourceIdentifier( String resourceIdentifier )
    {
        this.resourceIdentifier = resourceIdentifier;
    }

    public boolean isPattern()
    {
        return isPattern;
    }

    public void setPattern( boolean isPattern )
    {
        this.isPattern = isPattern;
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
