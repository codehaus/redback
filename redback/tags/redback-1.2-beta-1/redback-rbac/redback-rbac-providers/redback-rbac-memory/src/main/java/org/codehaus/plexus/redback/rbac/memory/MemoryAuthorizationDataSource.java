package org.codehaus.plexus.redback.rbac.memory;

import org.codehaus.plexus.redback.users.User;

/**
 * MemoryAuthorizationDataSource:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id:$
 */
public class MemoryAuthorizationDataSource
//    implements AuthorizationDataSource
{
    Object principal;

    User user;

    Object permission;

    public MemoryAuthorizationDataSource( Object principal, User user, Object permission )
    {
        this.principal = principal;
        this.user = user;
        this.permission = permission;
    }

    public Object getPrincipal()
    {
        return principal;
    }

    public void setPrincipal( String principal )
    {
        this.principal = principal;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser( User user )
    {
        this.user = user;
    }

    public Object getPermission()
    {
        return permission;
    }

    public void setPermission( Object permission )
    {
        this.permission = permission;
    }
}
