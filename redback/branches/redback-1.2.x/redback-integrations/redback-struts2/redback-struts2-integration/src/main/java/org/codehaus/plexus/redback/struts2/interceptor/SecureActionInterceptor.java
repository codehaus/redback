package org.codehaus.plexus.redback.struts2.interceptor;

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

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.redback.authorization.AuthorizationResult;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.system.SecuritySystemConstants;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * SecureActionInterceptor: Interceptor that will detect webwork actions that implement the SecureAction
 * interface and providing they do verify that the current user is authorized to execute the action
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @author Jesse McConnell <jesse@codehaus.org>
 * @version $Id: SecureActionInterceptor.java 4035 2006-09-14 12:59:40Z joakime $
 * @plexus.component role="com.opensymphony.xwork2.interceptor.Interceptor"
 * role-hint="redbackSecureActionInterceptor"
 */
public class SecureActionInterceptor
    extends AbstractHttpRequestTrackerInterceptor
{
    private static final String REQUIRES_AUTHORIZATION = "requires-authorization";

    private static final String REQUIRES_AUTHENTICATION = "requires-authentication";

    private static final String HTTP_HEADER_REFERER = "Referer";

    /**
     * @plexus.requirement
     */
    private SecuritySystem securitySystem;

    /**
     * @plexus.configuration default-value="simple"
     */
    private String trackerName;

    private String enableReferrerCheck;

    @Override
    public void destroy()
    {
    }

    protected String getTrackerName()
    {
        return trackerName;
    }

    public String getEnableReferrerCheck()
    {
        return enableReferrerCheck;
    }

    public void setEnableReferrerCheck( String enableReferrerCheck )
    {
        this.enableReferrerCheck = enableReferrerCheck;
    }

    /**
     * process the action to determine if it implements SecureAction and then act
     * accordingly
     *
     * @param invocation
     * @return
     * @throws Exception
     */
    @Override
    public String intercept( ActionInvocation invocation )
        throws Exception
    {
        ActionContext context = ActionContext.getContext();

        Action action = (Action) context.getActionInvocation().getAction();

        logger.debug( "SecureActionInterceptor: processing " + action.getClass().getName() );

        if( Boolean.valueOf( enableReferrerCheck ) )
        {
            logger.debug( "Referrer security check enabled." );
            executeReferrerSecurityCheck();
        }

        try
        {
            if ( action instanceof SecureAction )
            {
                SecureAction secureAction = (SecureAction) action;
                SecureActionBundle bundle = secureAction.getSecureActionBundle();

                if ( bundle == null )
                {
                    logger.error( "Null bundle detected." );

                    // TODO: send them somewhere else?
                    return invocation.invoke();
                }

                if ( bundle == SecureActionBundle.OPEN )
                {
                    logger.debug( "Bundle.OPEN detected." );

                    return invocation.invoke();
                }

                SecuritySession session =
                    (SecuritySession) context.getSession().get( SecuritySystemConstants.SECURITY_SESSION_KEY );

                // check the authentication requirements
                if ( bundle.requiresAuthentication() )
                {
                    if ( session == null || !session.isAuthenticated() )
                    {
                        logger.debug( "not authenticated, need to authenticate for this action" );
                        return processRequiresAuthentication( invocation );                        
                    }
                }

                List<SecureActionBundle.AuthorizationTuple> authzTuples = bundle.getAuthorizationTuples();

                // if operations are returned we need to perform authorization checks
                if ( authzTuples != null && authzTuples.size() > 0 )
                {
                    // authn adds a session, if there is no session they are not authorized and authn is required for
                    // authz, even if it is just a guest user
                    if ( session == null )
                    {
                        logger.debug( "session required for authorization to run" );
                        return processRequiresAuthentication( invocation );
                    }

                    for ( SecureActionBundle.AuthorizationTuple tuple : authzTuples )
                    {
                        logger.debug( "checking authz for " + tuple.toString() );

                        AuthorizationResult authzResult =
                            securitySystem.authorize( session, tuple.getOperation(), tuple.getResource() );

                        logger.debug( "checking the interceptor authz " + authzResult.isAuthorized() + " for " +
                            tuple.toString() );

                        if ( authzResult.isAuthorized() )
                        {
                            logger.debug( session.getUser().getPrincipal() + " is authorized for action " +
                                secureAction.getClass().getName() + " by " + tuple.toString() );
                            return invocation.invoke();
                        }
                    }

                    return processRequiresAuthorization( invocation );
                }
            }
            else
            {
                logger.debug( "SecureActionInterceptor: " + action.getClass().getName() + " not a secure action" );
            }
        }
        catch ( SecureActionException se )
        {
            logger.error( "can't generate the SecureActionBundle, deny access: " + se.getMessage() );
            return processRequiresAuthentication( invocation );
        }

        logger.debug( "not a secure action " + action.getClass().getName() );
        String result = invocation.invoke();
        logger.debug( "Passing invocation up, result is [" + result + "] on call " +
            invocation.getAction().getClass().getName() );
        return result;
    }

    private void executeReferrerSecurityCheck()
    {
        String referrer = ServletActionContext.getRequest().getHeader( HTTP_HEADER_REFERER );

        logger.debug( "HTTP Referer header: " + referrer );

        String[] tokens = StringUtils.splitPreserveAllTokens( referrer, "/", 3 );
        
        if( tokens != null )
        {
            String path;
            if( tokens.length < 3 )
            {
                path = referrer;
            }
            else
            {
                path = tokens[ tokens.length - 1 ];
            }

            logger.debug( "Calculated virtual path: " + path );

            ServletContext servletContext = ServletActionContext.getServletContext();

            String realPath = servletContext.getRealPath( path );

            if( StringUtils.isNotEmpty( realPath ) )
            {
                if( !realPath.endsWith( path ) )
                {
                    String errorMsg = "Failed referrer security check: Request did not come from the same server. " +
                        "Detected HTTP Referer header is '" + referrer + "'.";
                    logger.error( errorMsg );
                    throw new RuntimeException( errorMsg );
                }
                else
                {
                    logger.debug( "HTTP Referer header path found in server." );
                }
            }
        }
        else
        {
            logger.warn( "HTTP Referer header is null." );
        }
    }

    protected String processRequiresAuthorization( ActionInvocation invocation )
        throws ComponentLookupException
    {
        addActionInvocation( invocation ).setBackTrack();
        return REQUIRES_AUTHORIZATION;
    }
    
    protected String processRequiresAuthentication( ActionInvocation invocation )
        throws ComponentLookupException
    {
        HttpSession session = ServletActionContext.getRequest().getSession();
        
        if ( session != null )
        {
            session.removeAttribute( SecuritySystemConstants.SECURITY_SESSION_KEY );
        }
        
        addActionInvocation( invocation ).setBackTrack();
        return REQUIRES_AUTHENTICATION;
    }    
}
