package org.codehaus.plexus.redback.authentication.ldap;

/*
 * Copyright 2005 The Codehaus.
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

import javax.annotation.Resource;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.codehaus.plexus.redback.authentication.AuthenticationDataSource;
import org.codehaus.plexus.redback.authentication.AuthenticationException;
import org.codehaus.plexus.redback.authentication.AuthenticationResult;
import org.codehaus.plexus.redback.authentication.Authenticator;
import org.codehaus.plexus.redback.authentication.PasswordBasedAuthenticationDataSource;
import org.codehaus.plexus.redback.common.ldap.UserMapper;
import org.codehaus.plexus.redback.common.ldap.connection.LdapConnection;
import org.codehaus.plexus.redback.common.ldap.connection.LdapConnectionFactory;
import org.codehaus.plexus.redback.common.ldap.connection.LdapException;
import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * LdapBindAuthenticator:
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $Id$
 */
@Service("authenticator#ldap")
public class LdapBindAuthenticator
    implements Authenticator
{
    
    private Logger log = LoggerFactory.getLogger( getClass() );
    
    @Resource(name="userMapper#ldap")
    private UserMapper mapper;

    @Resource(name="ldapConnectionFactory#configurable")
    private LdapConnectionFactory connectionFactory;

    @Resource(name="userConfiguration")
    private UserConfiguration config;

    public String getId()
    {
        return "LdapBindAuthenticator";
    }

    public AuthenticationResult authenticate( AuthenticationDataSource s )
        throws AuthenticationException
    {
        PasswordBasedAuthenticationDataSource source = (PasswordBasedAuthenticationDataSource) s;

        if ( !config.getBoolean( "ldap.bind.authenticator.enabled" ) )
        {
            return new AuthenticationResult( false, source.getPrincipal(), null );
        }

        SearchControls ctls = new SearchControls();

        ctls.setCountLimit( 1 );

        ctls.setDerefLinkFlag( true );
        ctls.setSearchScope( SearchControls.SUBTREE_SCOPE );

        String filter = "(&(objectClass=" + mapper.getUserObjectClass() + ")"
            + ( mapper.getUserFilter() != null ? mapper.getUserFilter() : "" ) + "(" + mapper.getUserIdAttribute()
            + "=" + source.getPrincipal() + "))";

        log.info(
                          "Searching for users with filter: \'" + filter + "\'" + " from base dn: "
                              + mapper.getUserBaseDn() );

        LdapConnection ldapConnection = getLdapConnection();
        LdapConnection authLdapConnection = null;
        NamingEnumeration<SearchResult> results = null;
        try
        {
            DirContext context = ldapConnection.getDirContext();

            results = context.search( mapper.getUserBaseDn(), filter, ctls );

            log.info( "Found user?: " + results.hasMoreElements() );

            if ( results.hasMoreElements() )
            {
                SearchResult result = results.nextElement();

                String userDn = result.getNameInNamespace();

                log.info( "Attempting Authenication: + " + userDn );

                authLdapConnection = connectionFactory.getConnection( userDn, source.getPassword() );

                return new AuthenticationResult( true, source.getPrincipal(), null );
            }
            else
            {
                return new AuthenticationResult( false, source.getPrincipal(), null );
            }
        }
        catch ( LdapException e )
        {
            return new AuthenticationResult( false, source.getPrincipal(), e );
        }
        catch ( NamingException e )
        {
            return new AuthenticationResult( false, source.getPrincipal(), e );
        }
        finally
        {
            closeNamingEnumeration( results );
            closeLdapConnection( ldapConnection );
            if ( authLdapConnection != null )
            {
                closeLdapConnection( authLdapConnection );
            }
        }
    }

    public boolean supportsDataSource( AuthenticationDataSource source )
    {
        return ( source instanceof PasswordBasedAuthenticationDataSource );
    }

    private LdapConnection getLdapConnection()
    {
        try
        {
            return connectionFactory.getConnection();
        }
        catch ( LdapException e )
        {
            log.warn( "failed to get a ldap connection " + e.getMessage(), e );
            throw new RuntimeException( "failed to get a ldap connection " + e.getMessage(), e );
        }
    }

    private void closeLdapConnection( LdapConnection ldapConnection )
    {
        if ( ldapConnection != null )
        {
            ldapConnection.close();
        }
    }
    
    private void closeNamingEnumeration( NamingEnumeration<SearchResult> results )
    {
        try
        {
            if ( results != null )
            {
                results.close();
            }
        }
        catch ( NamingException e )
        {
            log.warn( "skip exception closing naming search result " + e.getMessage() );
        }
    }
}
