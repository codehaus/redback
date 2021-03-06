package org.codehaus.redback.intetgrations.struts2.it;

/*
 * Copyright 2013
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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PasswordResetTest
    extends AbstractSeleniumTestCase
{
    private static final String USERNAME = "resetuser";

    @BeforeClass
    public void createTestUser()
    {
        createUser( USERNAME, "password1", "Password Reset User", "resetuser@localhost" );
    }

    @Test
    public void testResetPassword()
        throws Exception
    {
        selenium.deleteAllVisibleCookies();
        selenium.open( "/security/passwordReset.action" );

        selenium.type( "passwordResetForm_username", USERNAME );
        submit();

        assert selenium.isTextPresent( "Request Password Reset" );
        assert selenium.isTextPresent( "If the user was found, a message has been sent." );
    }

    @Test
    public void testResetPasswordCancel()
        throws Exception
    {
        selenium.deleteAllVisibleCookies();
        selenium.open( "/security/passwordReset.action" );

        selenium.type( "passwordResetForm_username", USERNAME );
        cancel();

        assert selenium.isTextPresent( "Login" );
        assert selenium.isTextPresent( "Request a password reset." );
        assert !selenium.isTextPresent( "If the user was found, a message has been sent." );
    }

    @AfterClass
    public void deleteUser()
    {
        deleteUser( USERNAME );
    }
}
