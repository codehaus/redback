package org.codehaus.plexus.redback.management;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.codehaus.plexus.redback.rbac.jdo.RbacDatabase;
import org.codehaus.plexus.redback.rbac.jdo.io.stax.RbacJdoModelStaxReader;
import org.codehaus.plexus.redback.rbac.jdo.io.stax.RbacJdoModelStaxWriter;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManager;
import org.codehaus.plexus.redback.keys.jdo.AuthenticationKeyDatabase;
import org.codehaus.plexus.redback.keys.jdo.io.stax.RedbackKeyManagementJdoStaxReader;
import org.codehaus.plexus.redback.keys.jdo.io.stax.RedbackKeyManagementJdoStaxWriter;
import org.codehaus.plexus.redback.rbac.Operation;
import org.codehaus.plexus.redback.rbac.Permission;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.jdo.UserDatabase;
import org.codehaus.plexus.redback.users.jdo.io.stax.UsersManagementStaxReader;
import org.codehaus.plexus.redback.users.jdo.io.stax.UsersManagementStaxWriter;
import org.codehaus.plexus.util.IOUtil;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * JDO implementation of the data management tool.
 *
 * @todo do we really need JDO specifics here? Could optimize by going straight to JDOFactory
 * @todo check whether this current method logs everything unnecessarily.
 * @plexus.component role="org.codehaus.plexus.redback.management.DataManagementTool" role-hint="jdo"
 */
public class JdoDataManagementTool
    implements DataManagementTool
{
    private static final String USERS_XML_NAME = "users.xml";

    private static final String KEYS_XML_NAME = "keys.xml";

    private static final String RBAC_XML_NAME = "rbac.xml";

    public void backupRBACDatabase( RBACManager manager, File backupDirectory )
        throws RbacManagerException, IOException, XMLStreamException
    {
        RbacDatabase database = new RbacDatabase();
        database.setRoles( manager.getAllRoles() );
        database.setUserAssignments( manager.getAllUserAssignments() );
        database.setPermissions( manager.getAllPermissions() );
        database.setOperations( manager.getAllOperations() );
        database.setResources( manager.getAllResources() );

        RbacJdoModelStaxWriter writer = new RbacJdoModelStaxWriter();
        FileWriter fileWriter = new FileWriter( new File( backupDirectory, RBAC_XML_NAME ) );
        try
        {
            writer.write( fileWriter, database );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    public void backupUserDatabase( UserManager manager, File backupDirectory )
        throws IOException, XMLStreamException
    {
        UserDatabase database = new UserDatabase();
        database.setUsers( manager.getUsers() );

        UsersManagementStaxWriter writer = new UsersManagementStaxWriter();
        FileWriter fileWriter = new FileWriter( new File( backupDirectory, USERS_XML_NAME ) );
        try
        {
            writer.write( fileWriter, database );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    public void backupKeyDatabase( KeyManager manager, File backupDirectory )
        throws IOException, XMLStreamException
    {
        AuthenticationKeyDatabase database = new AuthenticationKeyDatabase();
        database.setKeys( manager.getAllKeys() );

        RedbackKeyManagementJdoStaxWriter writer = new RedbackKeyManagementJdoStaxWriter();
        FileWriter fileWriter = new FileWriter( new File( backupDirectory, KEYS_XML_NAME ) );
        try
        {
            writer.write( fileWriter, database );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    public void restoreRBACDatabase( RBACManager manager, File backupDirectory )
        throws IOException, XMLStreamException, RbacManagerException
    {
        RbacJdoModelStaxReader reader = new RbacJdoModelStaxReader();

        FileReader fileReader = new FileReader( new File( backupDirectory, RBAC_XML_NAME ) );

        RbacDatabase database;
        try
        {
            database = reader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        Map permissionMap = new HashMap();
        Map resources = new HashMap();
        Map operations = new HashMap();
        for ( Iterator i = database.getRoles().iterator(); i.hasNext(); )
        {
            Role role = (Role) i.next();

            // TODO: this could be generally useful and put into saveRole itself as long as the performance penalty isn't too harsh.
            //   Currently it always saves everything where it could pull pack the existing permissions, etc if they exist
            List permissions = new ArrayList();
            for ( Iterator j = role.getPermissions().iterator(); j.hasNext(); )
            {
                Permission permission = (Permission) j.next();

                if ( permissionMap.containsKey( permission.getName() ) )
                {
                    permission = (Permission) permissionMap.get( permission.getName() );
                }
                else if ( manager.permissionExists( permission ) )
                {
                    permission = manager.getPermission( permission.getName() );
                    permissionMap.put( permission.getName(), permission );
                }
                else
                {
                    Operation operation = permission.getOperation();
                    if ( operations.containsKey( operation.getName() ) )
                    {
                        operation = (Operation) operations.get( operation.getName() );
                    }
                    else if ( manager.operationExists( operation ) )
                    {
                        operation = manager.getOperation( operation.getName() );
                        operations.put( operation.getName(), operation );
                    }
                    else
                    {
                        operation = manager.saveOperation( operation );
                        operations.put( operation.getName(), operation );
                    }
                    permission.setOperation( operation );

                    Resource resource = permission.getResource();
                    if ( resources.containsKey( resource.getIdentifier() ) )
                    {
                        resource = (Resource) resources.get( resource.getIdentifier() );
                    }
                    else if ( manager.resourceExists( resource ) )
                    {
                        resource = manager.getResource( resource.getIdentifier() );
                        resources.put( resource.getIdentifier(), resource );
                    }
                    else
                    {
                        resource = manager.saveResource( resource );
                        resources.put( resource.getIdentifier(), resource );
                    }
                    permission.setResource( resource );

                    permission = manager.savePermission( permission );
                    permissionMap.put( permission.getName(), permission );
                }
                permissions.add( permission );
            }
            role.setPermissions( permissions );

            manager.saveRole( role );
        }

        for ( Iterator i = database.getUserAssignments().iterator(); i.hasNext(); )
        {
            UserAssignment userAssignment = (UserAssignment) i.next();

            manager.saveUserAssignment( userAssignment );
        }
    }

    public void restoreUsersDatabase( UserManager manager, File backupDirectory )
        throws IOException, XMLStreamException
    {
        UsersManagementStaxReader reader = new UsersManagementStaxReader();

        FileReader fileReader = new FileReader( new File( backupDirectory, USERS_XML_NAME ) );

        UserDatabase database;
        try
        {
            database = reader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        for ( Iterator i = database.getUsers().iterator(); i.hasNext(); )
        {
            User user = (User) i.next();

            manager.addUserUnchecked( user );
        }
    }

    public void restoreKeysDatabase( KeyManager manager, File backupDirectory )
        throws IOException, XMLStreamException
    {
        RedbackKeyManagementJdoStaxReader reader = new RedbackKeyManagementJdoStaxReader();

        FileReader fileReader = new FileReader( new File( backupDirectory, KEYS_XML_NAME ) );

        AuthenticationKeyDatabase database;
        try
        {
            database = reader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        for ( Iterator i = database.getKeys().iterator(); i.hasNext(); )
        {
            AuthenticationKey key = (AuthenticationKey) i.next();

            manager.addKey( key );
        }
    }

    public void eraseRBACDatabase( RBACManager manager )
    {
        manager.eraseDatabase();
    }

    public void eraseUsersDatabase( UserManager manager )
    {
        manager.eraseDatabase();
    }

    public void eraseKeysDatabase( KeyManager manager )
    {
        manager.eraseDatabase();
    }
}
