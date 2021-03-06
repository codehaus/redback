package org.codehaus.plexus.security.system;

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

import org.codehaus.plexus.security.authentication.AuthenticationDataSource;
import org.codehaus.plexus.security.authentication.AuthenticationException;
import org.codehaus.plexus.security.authorization.AuthorizationException;
import org.codehaus.plexus.security.authorization.AuthorizationResult;
import org.codehaus.plexus.security.keys.KeyManager;
import org.codehaus.plexus.security.policy.AccountLockedException;
import org.codehaus.plexus.security.policy.MustChangePasswordException;
import org.codehaus.plexus.security.policy.UserSecurityPolicy;
import org.codehaus.plexus.security.user.UserManager;
import org.codehaus.plexus.security.user.UserNotFoundException;

/**
 * SecuritySystem:
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $ID:$
 *
 */
public interface SecuritySystem
{
    static String ROLE = SecuritySystem.class.getName();

    // ----------------------------------------------------------------------------
    // Authentication
    // ----------------------------------------------------------------------------

    public SecuritySession authenticate( AuthenticationDataSource source )
        throws AuthenticationException, UserNotFoundException, AccountLockedException;

    public boolean isAuthenticated( AuthenticationDataSource source )
        throws AuthenticationException, UserNotFoundException, AccountLockedException;

    public String getAuthenticatorId();

    // ----------------------------------------------------------------------------
    // Authorization
    // ----------------------------------------------------------------------------

    public AuthorizationResult authorize( SecuritySession session, Object permission )
        throws AuthorizationException;

    public boolean isAuthorized( SecuritySession session, Object permission )
        throws AuthorizationException;

    public AuthorizationResult authorize( SecuritySession session, Object permission, Object resource )
        throws AuthorizationException;

    public boolean isAuthorized( SecuritySession session, Object permission, Object resource )
        throws AuthorizationException;

    String getAuthorizerId();

    // ----------------------------------------------------------------------------
    // User Management
    // ----------------------------------------------------------------------------

    public UserManager getUserManager();
    String getUserManagementId();
    
    // ----------------------------------------------------------------------------
    // Key Management
    // ----------------------------------------------------------------------------
    
    public KeyManager getKeyManager();
    String getKeyManagementId();

    // ----------------------------------------------------------------------------
    // Policy Management
    // ----------------------------------------------------------------------------
    
    public UserSecurityPolicy getPolicy();
    String getPolicyId();

    // ----------------------------------------------------------------------------
    // Configuration and Settings
    // ----------------------------------------------------------------------------
    
    public ApplicationDetails getApplicationDetails();
    public EmailSettings getEmailSettings();
}

