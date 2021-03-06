package org.codehaus.plexus.redback.xwork.interceptor;

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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * EnvironmentCheckInterceptor
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id: EnvironmentCheckInterceptor.java 4057 2006-09-15 23:43:16Z joakime $
 * @plexus.component role="com.opensymphony.xwork2.interceptor.Interceptor"
 * role-hint="redbackForceAdminUserInterceptor"
 */
public class ForceAdminUserInterceptor
    extends AbstractLogEnabled
    implements Interceptor
{
    private static final String SECURITY_ADMIN_USER_NEEDED = "security-admin-user-needed";

    private static boolean checked = false;

    /**
     * @plexus.requirement role-hint="configurable"
     */
    private UserManager userManager;

    /**
     * @plexus.requirement role-hint="default"
     */
    private RoleManager roleManager;
    
    /**
     * @plexus.requirement role-hint="default"
     */
    private UserConfiguration config;
    
    public void destroy()
    {
        // no-op
    }

    public void init()
    {

    }

    public String intercept( ActionInvocation invocation )
        throws Exception
    {
        if ( checked )
        {
            return invocation.invoke();
        }

        try
        {
            User user = userManager.findUser( config.getString( "redback.default.admin" ) );
            if ( user == null )
            {
                getLogger().info( "No admin user configured - forwarding to admin user creation page." );
                return SECURITY_ADMIN_USER_NEEDED;
            }
              
            roleManager.assignRole( "system-administrator", user.getPrincipal().toString() );
            
            checked = true;
            getLogger().info( "Admin user found. No need to configure admin user." );
            
        }
        catch ( UserNotFoundException e )
        {
            getLogger().info( "No admin user found - forwarding to admin user creation page." );
            return SECURITY_ADMIN_USER_NEEDED;
        }

        return invocation.invoke();
    }
}
