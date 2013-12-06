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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class RegisterTest
    extends AbstractSeleniumTestCase
{
    @Test
    public void testRegisterEmailValidation()
        throws Exception
    {
        selenium.open( "/reconfig.action?name=email.validation.required&value=true" );

        selenium.deleteAllVisibleCookies();
        selenium.open( "/" );

        selenium.click( "link=Register" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assert selenium.isTextPresent( "Register for an Account" );

        String name = "Registering User";
        selenium.type( "registerForm_user_username", "registeruser" );
        selenium.type( "registerForm_user_fullName", name );
        selenium.type( "registerForm_user_email", "reg1@localhost" );
        submit();

        assert selenium.isTextPresent( "Validation Reminder" );

        doLogin( "registeruser", "anything" );

        assert !selenium.isTextPresent( "You have entered an incorrect username and/or password." );
        assert !selenium.isElementPresent( "link=" + name );
        assertMainPage();
    }

    @Test
    public void testRegisterNoEmailValidation()
        throws Exception
    {
        selenium.open( "/reconfig.action?name=email.validation.required&value=false" );

        selenium.deleteAllVisibleCookies();
        selenium.open( "/" );

        selenium.click( "link=Register" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assert selenium.isTextPresent( "Register for an Account" );

        String username = "registeruser";
        String password = "password1";
        String name = "Registering User";
        selenium.type( "registerForm_user_username", username );
        selenium.type( "registerForm_user_fullName", name );
        selenium.type( "registerForm_user_email", "reg1@localhost" );
        selenium.type( "registerForm_user_password", password );
        selenium.type( "registerForm_user_confirmPassword", password );
        submit();

        assert !selenium.isTextPresent( "Validation Reminder" );
        assertMainPage();

        doLogin( username, password );

        assert selenium.isTextPresent( "Change Password" );
        assert selenium.isElementPresent( "link=" + name );
    }

    @Test
    public void testRegisterCancel()
        throws Exception
    {
        selenium.open( "/reconfig.action?name=email.validation.required&value=true" );

        selenium.deleteAllVisibleCookies();
        selenium.open( "/" );

        selenium.click( "link=Register" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assert selenium.isTextPresent( "Register for an Account" );

        selenium.type( "registerForm_user_username", "registeruser" );
        selenium.type( "registerForm_user_fullName", "Registering User" );
        selenium.type( "registerForm_user_email", "reg1@localhost" );
        cancel();

        assertMainPage();
    }

    @AfterMethod
    public void deleteUser()
    {
        deleteUserIfExists( "registeruser" );
    }
}
