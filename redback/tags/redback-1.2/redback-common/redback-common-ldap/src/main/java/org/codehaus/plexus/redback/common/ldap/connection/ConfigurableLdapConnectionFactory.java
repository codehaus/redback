package org.codehaus.plexus.redback.common.ldap.connection;

/*
 * The MIT License
 * Copyright (c) 2005, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.Properties;

import javax.annotation.Resource;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.StateFactory;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Service("ldapConnectionFactory#configurable")
public class ConfigurableLdapConnectionFactory
    implements LdapConnectionFactory, Initializable
{
    /**
     * @plexus.configuration
     */
    private String hostname;

    /**
     * @plexus.configuration
     */
    private int port;

    /**
     * @plexus.configuration
     */
    private String baseDn;

    /**
     * @plexus.configuration
     */
    private String contextFactory;

    /**
     * @plexus.configuration
     */
    private String bindDn;

    /**
     * @plexus.configuration
     */
    private String password;

    /**
     * @plexus.configuration
     */
    private String authenticationMethod;

    /**
     * @plexus.configuration
     */
    private Properties extraProperties;

    private LdapConnectionConfiguration configuration;


    @Resource(name="userConfiguration")
    private UserConfiguration userConf;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        try
        {
            configuration = new LdapConnectionConfiguration();
            configuration.setHostname( userConf.getString( "ldap.config.hostname", hostname ) );
            configuration.setPort( userConf.getInt( "ldap.config.port", port ) );
            configuration.setBaseDn( userConf.getConcatenatedList( "ldap.config.base.dn", baseDn ) );
            configuration.setContextFactory( userConf.getString( "ldap.config.context.factory", contextFactory ) );
            configuration.setBindDn( userConf.getConcatenatedList( "ldap.config.bind.dn", bindDn ) );
            configuration.setPassword( userConf.getString( "ldap.config.password", password ) );
            configuration.setAuthenticationMethod( userConf.getString( "ldap.config.authentication.method",
                                                                       authenticationMethod ) );
            configuration.setExtraProperties( extraProperties );
        }
        catch ( InvalidNameException e )
        {
            throw new InitializationException( "Error while initializing connection factory.", e );
        }
    }

    // ----------------------------------------------------------------------
    // LdapConnectionFactory Implementation
    // ----------------------------------------------------------------------

    public LdapConnection getConnection()
        throws LdapException
    {
        return new LdapConnection( configuration, null );
    }

    public LdapConnection getConnection( Rdn subRdn )
        throws LdapException
    {
        return new LdapConnection( configuration, subRdn );
    }

    public LdapConnection getConnection( String bindDn, String password )
        throws LdapException
    {
        return new LdapConnection( configuration, bindDn, password );
    }

    public LdapName getBaseDnLdapName()
        throws LdapException
    {
        try
        {
            return new LdapName( baseDn );
        }
        catch ( InvalidNameException e )
        {
            throw new LdapException( "The base DN is not a valid name.", e );
        }
    }

    public void addObjectFactory( Class<? extends ObjectFactory> objectFactoryClass )
    {
        configuration.getObjectFactories().add( objectFactoryClass );
    }

    public void addStateFactory( Class<? extends StateFactory> stateFactoryClass )
    {
        configuration.getStateFactories().add( stateFactoryClass );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public String toString()
    {
        return "{ConfigurableLdapConnectionFactory: configuration: " + configuration + "}";
    }

    public LdapConnectionConfiguration getConfiguration()
    {
        return configuration;
    }
}
