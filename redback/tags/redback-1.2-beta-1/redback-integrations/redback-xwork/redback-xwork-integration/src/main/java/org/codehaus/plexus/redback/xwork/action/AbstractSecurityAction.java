package org.codehaus.plexus.redback.xwork.action;

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

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.redback.policy.PasswordRuleViolationException;
import org.codehaus.plexus.redback.policy.PasswordRuleViolations;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystemConstants;
import org.codehaus.plexus.xwork.action.PlexusActionSupport;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import com.opensymphony.webwork.ServletActionContext;

/**
 * AbstractSecurityAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public abstract class AbstractSecurityAction
    extends PlexusActionSupport
    implements SecureAction
{
    protected static final String REQUIRES_AUTHENTICATION = "requires-authentication";

    private SecureActionBundle securityBundle;

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        if ( securityBundle == null )
        {
            securityBundle = initSecureActionBundle();
        }

        return securityBundle;
    }

    public abstract SecureActionBundle initSecureActionBundle()
        throws SecureActionException;

    protected void setAuthTokens( SecuritySession securitySession )
    {
        session.put( SecuritySystemConstants.SECURITY_SESSION_KEY, securitySession );
        this.setSession( session );
    }

    protected SecuritySession getSecuritySession()
    {
        return (SecuritySession) session.get( SecuritySystemConstants.SECURITY_SESSION_KEY );
    }

    // ------------------------------------------------------------------
    // Internal Support Methods
    // ------------------------------------------------------------------
    protected void processPasswordRuleViolations( PasswordRuleViolationException e )
    {
        processPasswordRuleViolations( e, "user.password" );
    }

    protected void processPasswordRuleViolations( PasswordRuleViolationException e, String field )
    {
        PasswordRuleViolations violations = e.getViolations();

        if ( violations != null )
        {
            Iterator it = violations.getLocalizedViolations().iterator();
            while ( it.hasNext() )
            {
                String violation = (String) it.next();
                addFieldError( field, violation );
            }
        }
    }

    protected String getBaseUrl()
    {
        HttpServletRequest req = ServletActionContext.getRequest();
        return req.getScheme() + "://" + req.getServerName() +
            ( req.getServerPort() == 80 ? "" : ":" + req.getServerPort() ) + req.getContextPath();
    }
}
