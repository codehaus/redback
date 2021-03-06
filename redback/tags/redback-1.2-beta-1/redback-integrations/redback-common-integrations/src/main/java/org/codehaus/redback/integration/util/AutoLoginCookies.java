package org.codehaus.redback.integration.util;

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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManager;
import org.codehaus.plexus.redback.keys.KeyManagerException;
import org.codehaus.plexus.redback.keys.KeyNotFoundException;
import org.codehaus.plexus.redback.policy.CookieSettings;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.util.StringUtils;

/**
 * AutoLoginCookies
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.redback.integration.util.AutoLoginCookies"
 */
public class AutoLoginCookies
    extends AbstractLogEnabled
{
    /**
     * @plexus.requirement
     */
    private SecuritySystem securitySystem;

    /**
     * Cookie key for the Remember Me functionality.
     */
    private static final String REMEMBER_ME_KEY = "rbkRememberMe";

    /**
     * Cookie key for the signon cookie.
     */
    private static final String SIGNON_KEY = "rbkSignon";

    public AuthenticationKey getRememberMeKey(HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest )
    {
        if ( !isRememberMeEnabled() )
        {
            return null;
        }

        Cookie rememberMeCookie = getCookie( httpServletRequest, REMEMBER_ME_KEY );

        if ( rememberMeCookie == null )
        {
            getLogger().debug( "Remember Me Cookie Not Found: " + REMEMBER_ME_KEY );
            return null;
        }

        // Found user with a remember me key.
        String providedKey = rememberMeCookie.getValue();

        getLogger().debug( "Found remember me cookie : " + providedKey );

        CookieSettings settings = securitySystem.getPolicy().getRememberMeCookieSettings();
        return findAuthKey( REMEMBER_ME_KEY, providedKey, settings.getDomain(), settings.getPath(), httpServletResponse, httpServletRequest );
    }

    public void setRememberMeCookie( String principal, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest )
    {
        if ( !isRememberMeEnabled() )
        {
            return;
        }

        try
        {
            CookieSettings settings = securitySystem.getPolicy().getRememberMeCookieSettings();
            int timeout = settings.getCookieTimeout();
            KeyManager keyManager = securitySystem.getKeyManager();
            AuthenticationKey authkey = keyManager.createKey( principal, "Remember Me Key", timeout );

            Cookie cookie = createCookie( REMEMBER_ME_KEY, authkey.getKey(), settings.getDomain(), settings.getPath(), httpServletRequest );
            if ( timeout > 0 )
            {
                cookie.setMaxAge( timeout );
            }
            httpServletResponse.addCookie( cookie );

        }
        catch ( KeyManagerException e )
        {
            getLogger().warn( "Unable to set remember me cookie." );
        }
    }

    public void removeRememberMeCookie( HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest )
    {
        CookieSettings settings = securitySystem.getPolicy().getRememberMeCookieSettings();
        removeCookie( httpServletResponse, httpServletRequest, REMEMBER_ME_KEY, settings.getDomain(), settings.getPath() );
    }

    public AuthenticationKey getSignonKey( HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest )
    {
        Cookie ssoCookie = getCookie( httpServletRequest, SIGNON_KEY );

        if ( ssoCookie == null )
        {
            getLogger().debug( "Single Sign On Cookie Not Found: " + SIGNON_KEY );
            return null;
        }

        // Found user with a single sign on key.

        String providedKey = ssoCookie.getValue();

        getLogger().debug( "Found sso cookie : " + providedKey );

        CookieSettings settings = securitySystem.getPolicy().getSignonCookieSettings();
        return findAuthKey( SIGNON_KEY, providedKey, settings.getDomain(), settings.getPath(), httpServletResponse, httpServletRequest );
    }

    public void setSignonCookie( String principal, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest )
    {
        try
        {
            CookieSettings settings = securitySystem.getPolicy().getSignonCookieSettings();
            int timeout = settings.getCookieTimeout();
            KeyManager keyManager = securitySystem.getKeyManager();
            AuthenticationKey authkey = keyManager.createKey( principal, "Signon Session Key", timeout );

            /* The path must remain as "/" in order for SSO to work on installations where the only
             * all of the servers are installed into the same web container but under different 
             * web contexts.
             */
            Cookie cookie = createCookie( SIGNON_KEY, authkey.getKey(), settings.getDomain(), settings.getPath(), httpServletRequest );
            if ( timeout > 0 )
            {
                cookie.setMaxAge( timeout );
            }
            httpServletResponse.addCookie( cookie );

        }
        catch ( KeyManagerException e )
        {
            getLogger().warn( "Unable to set single sign on cookie." );

        }
    }

    public void removeSignonCookie( HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest )
    {
        CookieSettings settings = securitySystem.getPolicy().getSignonCookieSettings();
        removeCookie( httpServletResponse, httpServletRequest, SIGNON_KEY, settings.getDomain(), settings.getPath() );
    }

    private static String getWebappContext( HttpServletRequest httpRequest )
    {
        // Calculate the webapp context.
        String webappContext = httpRequest.getContextPath();

        if ( StringUtils.isEmpty( webappContext ) )
        {
            // Still empty?  means you are a root context.
            webappContext = "/";
        }

        return webappContext;
    }

    public boolean isRememberMeEnabled()
    {
        return securitySystem.getPolicy().getRememberMeCookieSettings().isEnabled();
    }

    private AuthenticationKey findAuthKey( String cookieName, String providedKey, String domain, String path,
                                           HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest )
    {
        try
        {
            AuthenticationKey authkey = securitySystem.getKeyManager().findKey( providedKey );

            getLogger().debug( "Found AuthKey: " + authkey );

            return authkey;
        }
        catch ( KeyNotFoundException e )
        {
            getLogger().info( "Invalid AuthenticationKey " + providedKey + " submitted. Invalidating cookie." );

            // Invalid Cookie.  Remove it.
            removeCookie( httpServletResponse, httpServletRequest, cookieName, domain, path );
        }
        catch ( KeyManagerException e )
        {
            getLogger().error( "KeyManagerException: " + e.getMessage(), e );
        }

        return null;
    }

    private static Cookie getCookie( HttpServletRequest request, String name )
    {
        Cookie[] cookies = request.getCookies();

        Cookie cookie = null;
        if ( cookies != null && !StringUtils.isEmpty( name ) )
        {
            for ( int i = 0; i < cookies.length && cookie == null; i++ )
            {
                if ( StringUtils.equals( name, cookies[i].getName() ) )
                {
                    cookie = cookies[i];
                }
            }
        }

        return cookie;
    }

    private static void removeCookie( HttpServletResponse response, HttpServletRequest httpRequest, String cookieName, String domain, String path )
    {
        Cookie cookie = createCookie( cookieName, "", domain, path, httpRequest );
        cookie.setMaxAge( 0 );
        response.addCookie( cookie );
    }

    private static Cookie createCookie( String cookieName, String value, String domain, String path, HttpServletRequest httpRequest )
    {
        Cookie cookie = new Cookie( cookieName, value );
        if ( domain != null )
        {
            cookie.setDomain( domain );
        }
        if ( path != null )
        {
            cookie.setPath( path );
        }
        else
        {
            // default to the context path, otherwise you get /security and such in some places
            cookie.setPath( getWebappContext( httpRequest ) );
        }
        return cookie;
    }
}
