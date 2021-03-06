package org.codehaus.plexus.security.ui.web.action;

/*
 * Copyright 2001-2006 The Codehaus.
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

import com.opensymphony.webwork.dispatcher.SessionMap;
import com.opensymphony.webwork.interceptor.ServletRequestAware;
import com.opensymphony.webwork.interceptor.ServletResponseAware;

import org.codehaus.plexus.security.system.SecuritySystemConstants;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;
import org.codehaus.plexus.security.ui.web.util.CookieUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LogoutAction 
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * 
 * @plexus.component role="com.opensymphony.xwork.Action"
 *                   role-hint="pss-logout"
 *                   instantiation-strategy="per-lookup"
 */
public class LogoutAction
    extends AbstractAuthenticationAction
    implements ServletRequestAware, ServletResponseAware
{
    // Result Names.
    private static final String LOGOUT = "security-logout";

    private HttpServletRequest request;

    private HttpServletResponse response;

    public String logout()
    {
        // Invalidate session tokens.
        setAuthTokens( null );

        // Invalidate remember me cookie.
        CookieUtils.removeCookie( request, response, SecuritySystemConstants.REMEMBER_ME_KEY );

        // Invalidate sso cookie.
        CookieUtils.removeCookie( request, response, SecuritySystemConstants.SINGLE_SIGN_ON_KEY );

        if ( session != null )
        {
            ( (SessionMap) session ).invalidate();
        }

        return LOGOUT;
    }

    public SecureActionBundle initSecureActionBundle()
        throws SecureActionException
    {
        return SecureActionBundle.OPEN;
    }

    public void setServletRequest( HttpServletRequest request )
    {
        this.request = request;
    }

    public void setServletResponse( HttpServletResponse response )
    {
        this.response = response;
    }
}
