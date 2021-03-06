package org.codehaus.plexus.redback.authentication.users;

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
import java.util.Calendar;
import java.util.Date;

import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.redback.authentication.AuthenticationException;
import org.codehaus.plexus.redback.authentication.AuthenticationResult;
import org.codehaus.plexus.redback.authentication.Authenticator;
import org.codehaus.plexus.redback.authentication.PasswordBasedAuthenticationDataSource;
import org.codehaus.plexus.redback.policy.AccountLockedException;
import org.codehaus.plexus.redback.policy.MustChangePasswordException;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;

/**
 * Tests for {@link UserManagerAuthenticator} implementation.
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 */
public class UserManagerAuthenticatorTest
    extends PlexusInSpringTestCase
{
    /**
     * @plexus.requirement
     */
    private UserSecurityPolicy userSecurityPolicy;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        userSecurityPolicy = (UserSecurityPolicy) lookup( UserSecurityPolicy.ROLE );
        userSecurityPolicy.setEnabled( false );
    }

    public void testLookup()
        throws Exception
    {
        Authenticator component = (Authenticator) lookup( Authenticator.ROLE, "user-manager" );
        assertNotNull( component );
        assertEquals( UserManagerAuthenticator.class.getName(), component.getClass().getName() );
    }

    public void testAuthenticate()
        throws Exception
    {
        // Set up a few users for the Authenticator
        
        UserManager um = (UserManager) lookup( UserManager.ROLE, "memory" );
        User user = um.createUser( "test", "Test User", "testuser@somedomain.com" );
        user.setPassword( "testpass" );
        um.addUser( user );

        user = um.createUser( "guest", "Guest User", "testuser@somedomain.com" );
        user.setPassword( "guestpass" );
        um.addUser( user );

        user = um.createUser( "anonymous", "Anonymous User", "testuser@somedomain.com" );
        user.setPassword( "nopass" );
        um.addUser( user );

        // test with valid credentials
        Authenticator auth = (Authenticator) lookup( Authenticator.ROLE, "user-manager" );
        assertNotNull( auth );

        AuthenticationResult result = auth.authenticate( createAuthDataSource( "anonymous", "nopass" ) );
        assertTrue( result.isAuthenticated() );

        // test with invalid password
        result = auth.authenticate( createAuthDataSource( "anonymous", "wrongpass" ) );
        assertFalse( result.isAuthenticated() );
        assertNull( result.getException() );

        // test with unknown user
        result = auth.authenticate( createAuthDataSource( "unknownuser", "wrongpass" ) );
        assertFalse( result.isAuthenticated() );
        assertNotNull( result.getException() );
        assertEquals( result.getException().getClass().getName(), UserNotFoundException.class.getName() );
    }

    public void testAuthenticateLockedPassword()
        throws AuthenticationException, MustChangePasswordException, UserNotFoundException
    {
        userSecurityPolicy.setEnabled( true );

        // Set up a user for the Authenticator
        UserManager um = (UserManager) lookup( UserManager.ROLE, "memory" );
        User user = um.createUser( "testuser", "Test User Locked Password", "testuser@somedomain.com" );
        user.setPassword( "correctpass1" );
        user.setValidated( true );
        user.setPasswordChangeRequired( false );
        um.addUser( user );

        Authenticator auth = (Authenticator) lookup( Authenticator.ROLE, "user-manager" );
        assertNotNull( auth );

        boolean hasException = false;
        AuthenticationResult result = null;

        try
        {
            // test password lock
            for ( int i = 0; i < 11; i++ )
            {
                result = auth.authenticate( createAuthDataSource( "testuser", "wrongpass" ) );
            }
        }
        catch ( AccountLockedException e )
        {
            hasException = true;
        }
        finally
        {
            assertNotNull( result );
            assertFalse( result.isAuthenticated() );
            assertTrue( hasException );
        }
    }

    public void testAuthenticateExpiredPassword()
        throws AuthenticationException, AccountLockedException, UserNotFoundException
    {
        userSecurityPolicy.setEnabled( true );
        userSecurityPolicy.setPasswordExpirationDays( 15 );

        // Set up a user for the Authenticator
	UserManager um = (UserManager) lookup( UserManager.ROLE, "memory" );
        User user = um.createUser( "testuser", "Test User Expired Password", "testuser@somedomain.com" );
        user.setPassword( "expiredpass1" );
        user.setValidated( true );
        user.setPasswordChangeRequired( false );
        um.addUser( user );

        Authenticator auth = (Authenticator) lookup( Authenticator.ROLE, "user-manager" );
        assertNotNull( auth );

        boolean hasException = false;

        try
        {
            // test successful authentication
            AuthenticationResult result = auth.authenticate( createAuthDataSource( "testuser", "expiredpass1" ) );
            assertTrue( result.isAuthenticated() );

            // test expired password
            user = um.findUser( "testuser" );

            Calendar currentDate = Calendar.getInstance();
            currentDate.set( Calendar.YEAR, currentDate.get( Calendar.YEAR ) - 1 );
            Date lastPasswordChange = currentDate.getTime();
            user.setLastPasswordChange( lastPasswordChange );

            um.updateUser( user );

            auth.authenticate( createAuthDataSource( "testuser", "expiredpass1" ) );
        }
        catch ( MustChangePasswordException e )
        {
            hasException = true;
        }
        finally
        {
            assertTrue( hasException );
        }
    }

    private PasswordBasedAuthenticationDataSource createAuthDataSource(String username, String password)
    {
        PasswordBasedAuthenticationDataSource source = new PasswordBasedAuthenticationDataSource();
        
        source.setPrincipal( username );
        source.setPassword( password );
        
        return source;
        
    }
}
