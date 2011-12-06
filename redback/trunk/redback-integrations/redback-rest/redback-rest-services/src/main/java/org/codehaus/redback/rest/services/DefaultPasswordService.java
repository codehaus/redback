package org.codehaus.redback.rest.services;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManagerException;
import org.codehaus.plexus.redback.policy.PasswordEncoder;
import org.codehaus.plexus.redback.policy.PasswordRuleViolationException;
import org.codehaus.plexus.redback.policy.PasswordRuleViolations;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.redback.integration.filter.authentication.HttpAuthenticator;
import org.codehaus.redback.rest.api.model.ErrorMessage;
import org.codehaus.redback.rest.api.services.PasswordService;
import org.codehaus.redback.rest.api.services.RedbackServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Olivier Lamy
 * @since 1.4
 */
@Service( "passwordService#rest" )
public class DefaultPasswordService
    implements PasswordService
{

    private Logger log = LoggerFactory.getLogger( getClass() );

    private SecuritySystem securitySystem;

    private HttpAuthenticator httpAuthenticator;

    @Context
    private HttpServletRequest httpServletRequest;

    @Inject
    public DefaultPasswordService( SecuritySystem securitySystem,
                                   @Named( "httpAuthenticator#basic" ) HttpAuthenticator httpAuthenticator )
    {
        this.securitySystem = securitySystem;
        this.httpAuthenticator = httpAuthenticator;
    }

    public Boolean changePasswordWithKey( String password, String passwordConfirmation, String key )
        throws RedbackServiceException
    {
        Boolean isLogged = httpAuthenticator.getSecuritySession( httpServletRequest.getSession( true ) ) != null;

        //RedbackRequestInformation redbackRequestInformation = RedbackAuthenticationThreadLocal.get();

        String principal = null;

        if ( !isLogged )
        {
            throw new RedbackServiceException( "you must be logged", Response.Status.FORBIDDEN.getStatusCode() );
        }

        if ( StringUtils.isEmpty( password ) )
        {
            throw new RedbackServiceException( "password cannot be empty", Response.Status.FORBIDDEN.getStatusCode() );
        }
        if ( StringUtils.isEmpty( passwordConfirmation ) )
        {
            throw new RedbackServiceException( "password confirmation cannot be empty",
                                               Response.Status.FORBIDDEN.getStatusCode() );
        }
        if ( !StringUtils.equals( password, passwordConfirmation ) )
        {
            throw new RedbackServiceException( "password confirmation must be same as password",
                                               Response.Status.FORBIDDEN.getStatusCode() );
        }

        try
        {
            AuthenticationKey authKey = securitySystem.getKeyManager().findKey( key );

            principal = authKey.getForPrincipal();

            // password validation with a tmp user
            User tempUser = securitySystem.getUserManager().createUser( "temp", "temp", "temp" );
            tempUser.setPassword( password );
            securitySystem.getPolicy().validatePassword( tempUser );

            PasswordEncoder encoder = securitySystem.getPolicy().getPasswordEncoder();

            User user = securitySystem.getUserManager().findUser( principal );
            String encodedPassword = encoder.encodePassword( password );
            user.setEncodedPassword( encodedPassword );
            user.setPassword( password );

            securitySystem.getPolicy().validatePassword( user );
            securitySystem.getUserManager().updateUser( user );

        }
        catch ( KeyManagerException e )
        {
            log.info( "issue to find key {}: {}", key, e.getMessage() );
            throw new RedbackServiceException( "issue with key", Response.Status.FORBIDDEN.getStatusCode() );
        }
        catch ( UserNotFoundException e )
        {
            log.info( "user {} not found", e.getMessage() );
            List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>( 2 );
            ErrorMessage errorMessage = new ErrorMessage( "cannot.update.user.not.found", new String[]{ principal } );
            errorMessages.add( errorMessage );
            errorMessage = new ErrorMessage( "admin.deleted.account" );
            errorMessages.add( errorMessage );
            throw new RedbackServiceException( errorMessages );
        }
        catch ( PasswordRuleViolationException e )
        {
            PasswordRuleViolations violations = e.getViolations();
            List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>( violations.getViolations().size() );
            if ( violations != null )
            {
                for ( String violation : violations.getLocalizedViolations() )
                {
                    errorMessages.add( new ErrorMessage( violation ) );
                }
            }
            throw new RedbackServiceException( errorMessages );
        }

        return true;
    }
}