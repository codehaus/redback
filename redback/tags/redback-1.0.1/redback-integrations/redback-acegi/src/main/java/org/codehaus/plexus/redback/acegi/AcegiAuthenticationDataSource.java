/**
 *
 */
package org.codehaus.plexus.redback.acegi;

import org.codehaus.plexus.redback.authentication.AuthenticationDataSource;
import org.codehaus.plexus.redback.authentication.PasswordBasedAuthenticationDataSource;

import java.util.Map;

/**
 * {@link AuthenticationDataSource} implementation that wraps the authentication
 * credentials in a token map.<p>
 * The token map is used by the {@link AcegiAuthenticator} to create a an Acegi
 * authentication token to be used by the respective Acegi authentication
 * provider for authentication.
 *
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
public class AcegiAuthenticationDataSource
    extends PasswordBasedAuthenticationDataSource
{
    private static final String TOKEN_USERNAME = "username";

    private static final String TOKEN_PASSWORD = "password";

    /**
     * Store for credential tokens.
     */
    private Map tokenMap;

    public AcegiAuthenticationDataSource( Map tokenMap )
    {
        this.tokenMap = tokenMap;
    }

    /**
     * @return the tokenMap
     */
    public Map getTokenMap()
    {
        return tokenMap;
    }

    /**
     * @param tokenMap the tokenMap to set
     */
    public void setTokenMap( Map tokenMap )
    {
        this.tokenMap = tokenMap;
    }

    public String getPassword()
    {
        return (String) this.tokenMap.get( TOKEN_PASSWORD );
    }

    public String getUsername()
    {
        return (String) this.tokenMap.get( TOKEN_USERNAME );
    }

}
