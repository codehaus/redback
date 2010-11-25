package org.codehaus.plexus.redback.struts2.action.admin;

/*
 * Copyright 2008 The Codehaus.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.redback.authentication.AuthenticationResult;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.RbacObjectInvalidException;
import org.codehaus.plexus.redback.rbac.Role;

import com.opensymphony.xwork2.Action;
import org.codehaus.plexus.redback.system.DefaultSecuritySession;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystemConstants;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.memory.SimpleUser;
import org.codehaus.redback.integration.model.AdminEditUserCredentials;

/**
 * @todo missing tests for success/fail on standard show/edit functions (non security testing related)
 */
public class UserEditActionTest
    extends AbstractUserCredentialsActionTest
{
    private UserEditAction action;

    public void setUp()
        throws Exception
    {
        super.setUp();

        action = (UserEditAction) lookup( Action.class, "redback-admin-user-edit" );

        login( action, "user", PASSWORD );
    }

    public void testEditPageShowsAdministratableRoles()
        throws RbacObjectInvalidException, RbacManagerException
    {
        addAssignment( "user", "User Administrator" );

        addAssignment( "user2", "Project Administrator - default" );
        addAssignment( "user2", "Project Administrator - other" );

        action.setUsername( "user2" );
        assertEquals( Action.INPUT, action.edit() );

        List<Role> effectivelyAssignedRoles = action.getEffectivelyAssignedRoles();
        assertEquals( 2, effectivelyAssignedRoles.size() );
        Role r = effectivelyAssignedRoles.get( 0 );
        assertEquals( "Project Administrator - default", r.getName() );
        r = effectivelyAssignedRoles.get( 1 );
        assertEquals( "Project Administrator - other", r.getName() );
        assertFalse( action.isHasHiddenRoles() );
    }

    public void testEditPageHidesUnadministratableRoles()
        throws RbacObjectInvalidException, RbacManagerException
    {
        // REDBACK-29
        // user should not be able to see the other project admin role of user2, but should be able to see the one
        // from their own group

        addAssignment( "user", "Project Administrator - default" );

        addAssignment( "user2", "Project Administrator - default" );
        addAssignment( "user2", "Project Administrator - other" );

        action.setUsername( "user2" );
        assertEquals( Action.INPUT, action.edit() );

        List<Role> effectivelyAssignedRoles = action.getEffectivelyAssignedRoles();
        assertEquals( 1, effectivelyAssignedRoles.size() );
        Role r = effectivelyAssignedRoles.get( 0 );
        assertEquals( "Project Administrator - default", r.getName() );
        assertTrue( action.isHasHiddenRoles() );
    }

    public void testEditPageHidesUnassignableRoles()
        throws RbacObjectInvalidException, RbacManagerException
    {
        // REDBACK-201
        // user should not be able to see the unassignable roles 

        addAssignment( "user", "User Administrator" );

        action.setUsername( "user" );
        assertEquals( Action.INPUT, action.edit() );

        List<Role> effectivelyAssignedRoles = action.getEffectivelyAssignedRoles();
        assertEquals( 1, effectivelyAssignedRoles.size() );
        Role r = effectivelyAssignedRoles.get( 0 );
        assertEquals( "User Administrator", r.getName() );
        assertFalse( action.isHasHiddenRoles() );
    }

    public void testRequireOldPWWhenEditingOwnAccountSuccess()
        throws Exception
    {
        addAssignment( "user", "User Administrator" );

        action.setUsername( "user" );
        assertEquals( Action.INPUT, action.edit() );

        assertTrue( action.isSelf() );

        AdminEditUserCredentials user = action.getUser();
        user.setEmail( "user@example.com" );
        user.setFullName( "User" );
        action.setOldPassword( PASSWORD );

        Map<String, Object> mockSession = new HashMap<String, Object>();
        
        User currentUser = new SimpleUser();
        currentUser.setUsername( "user" );

        AuthenticationResult authResult = new AuthenticationResult( true, "user", null );
        SecuritySession securitySession = new DefaultSecuritySession( authResult, currentUser );

        mockSession.put( SecuritySystemConstants.SECURITY_SESSION_KEY, securitySession );
        action.setSession( mockSession );

        assertEquals( Action.SUCCESS , action.submit() );

        assertEquals( 0, action.getFieldErrors().size() );
    }

    public void testRequireOldPWWhenEditingOwnAccountFailed()
        throws Exception
    {
        addAssignment( "user", "User Administrator" );

        action.setUsername( "user" );
        assertEquals( Action.INPUT, action.edit() );

        assertTrue( action.isSelf() );

        AdminEditUserCredentials user = action.getUser();
        user.setEmail( "user@example.com" );
        user.setFullName( "User" );
        user.setPassword( PASSWORD );
        user.setConfirmPassword( PASSWORD );
        
        action.setOldPassword( "notmatchingoldpassword" );

        assertEquals( Action.ERROR , action.submit() );

        Map<String, List<String>> fieldErrors = action.getFieldErrors();
        List<String> oldPasswordErrors = fieldErrors.get( "oldPassword" );

        assertNotNull( oldPasswordErrors );
        assertEquals( 1, oldPasswordErrors.size() );

        assertEquals( action.getText( "password.provided.does.not.match.existing" ), oldPasswordErrors.get( 0 ) );
    }

    public void testRequireOldPWWhenEditingOwnAccountOldPasswordIsNull()
        throws Exception
    {
        addAssignment( "user", "User Administrator" );

        action.setUsername( "user" );
        assertEquals( Action.INPUT, action.edit() );

        assertTrue( action.isSelf() );

        AdminEditUserCredentials user = action.getUser();
        user.setEmail( "user@example.com" );
        user.setFullName( "User" );
        user.setPassword( PASSWORD );
        user.setConfirmPassword( PASSWORD );

        action.setOldPassword( null );
        
        assertEquals( Action.ERROR , action.submit() );

        Map<String, List<String>> fieldErrors = action.getFieldErrors();
        List<String> oldPasswordErrors = fieldErrors.get( "oldPassword" );

        assertNotNull( oldPasswordErrors );
        assertEquals( 1, oldPasswordErrors.size() );

        assertEquals( action.getText( "old.password.required" ), oldPasswordErrors.get( 0 ) );    
    }

    public void testRequireAdminPWWhenEditingOtherAccountPWIncorrect()
        throws Exception
    {
        addAssignment( "user", "User Administrator" );

        action.setUsername( "user2" );

        assertEquals( Action.INPUT, action.edit() );

        assertFalse( action.isSelf() );

        AdminEditUserCredentials user = action.getUser();
        user.setEmail( "user2@example.com" );
        user.setFullName( "User2" );
        user.setPassword( PASSWORD );
        user.setConfirmPassword( PASSWORD );

        assertEquals( UserEditAction.CONFIRM , action.submit() );

        assertFalse( action.isSelf() );

        action.setUserAdminPassword( "boguspassword" );

        assertEquals( UserEditAction.CONFIRM_ERROR, action.confirmAdminPassword() );
        
        Collection<String> errors = action.getActionErrors();

        assertNotNull( errors );
        assertEquals( 1, errors.size() );

        assertEquals( action.getText( "user.admin.password.does.not.match.existing" ), errors.iterator().next() );
    }

    public void testRequireAdminPWWhenEditingOtherAccountPWEmpty()
        throws Exception
    {
        addAssignment( "user", "User Administrator" );

        action.setUsername( "user2" );
        assertEquals( Action.INPUT, action.edit() );

        assertFalse( action.isSelf() );

        AdminEditUserCredentials user = action.getUser();
        user.setEmail( "user2@example.com" );
        user.setFullName( "User2" );
        user.setPassword( PASSWORD );
        user.setConfirmPassword( PASSWORD );

        action.setUserAdminPassword( "" );

        assertEquals( UserEditAction.CONFIRM, action.submit() );

        assertFalse( action.isSelf() );

        assertEquals( UserEditAction.CONFIRM_ERROR, action.confirmAdminPassword() );

        Collection<String> errors = action.getActionErrors();

        assertNotNull( errors );
        assertEquals( 1, errors.size() );

        assertEquals( action.getText( "user.admin.password.required" ), errors.iterator().next() );
    }

}
