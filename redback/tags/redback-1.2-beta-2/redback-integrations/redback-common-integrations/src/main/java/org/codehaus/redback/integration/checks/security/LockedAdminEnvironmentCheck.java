package org.codehaus.redback.integration.checks.security;

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
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.system.check.EnvironmentCheck;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.redback.integration.role.RoleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * LockedAdminEnvironmentCheck: checks if accounts marked as system administrator are locked
 * and unlocks them on startup.
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $Id$
 */
@Service("environmentCheck#locked-admin-check")
public class LockedAdminEnvironmentCheck
    implements EnvironmentCheck
{
    
    protected Logger log = LoggerFactory.getLogger( getClass() );

    @Resource(name="userManager#configurable")
    private UserManager userManager;

    @Resource(name="rBACManager#cached")
    private RBACManager rbacManager;

    /**
     * boolean detailing if this environment check has been executed
     */
    private boolean checked = false;

    /**
     * This environment check will unlock system administrator accounts that are locked on the restart of the
     * application when the environment checks are processed.
     * 
     * @param violations
     */
    public void validateEnvironment( List violations )
    {
        if ( !checked && !userManager.isReadOnly() )
        {
            List<String> roles = new ArrayList<String>();
            roles.add( RoleConstants.SYSTEM_ADMINISTRATOR_ROLE );

            List systemAdminstrators;
            try
            {
                systemAdminstrators = rbacManager.getUserAssignmentsForRoles( roles );
                
                for ( Iterator i = systemAdminstrators.iterator(); i.hasNext(); )
                {
                    UserAssignment userAssignment = (UserAssignment)i.next();
                    
                    try
                    {
                        User admin = userManager.findUser( userAssignment.getPrincipal() );
                        
                        if ( admin.isLocked() )
                        {
                            log.info( "Found locked system administrator: " + admin.getUsername() );
                            log.info(  "Unlocking system administrator: " + admin.getUsername() );
                            admin.setLocked( false );
                            userManager.updateUser( admin );
                        }
                    }
                    catch ( UserNotFoundException ne )
                    {
                        log.warn( "Dangling UserAssignment -> " + userAssignment.getPrincipal() );
                        violations.add( ne );
                    }
                }
            }
            catch ( RbacManagerException e )
            {                
                violations.add( e );
            }
            
            checked = true;
        }
    }
}
