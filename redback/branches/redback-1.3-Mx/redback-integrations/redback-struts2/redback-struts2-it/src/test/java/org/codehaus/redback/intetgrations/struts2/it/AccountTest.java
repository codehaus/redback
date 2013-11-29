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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AccountTest
    extends AbstractSeleniumTestCase
{
    private static final String USERNAME = "testuser";

    private static final String PASSWORD = "testuser1";

    public static final String NAME = "Test User";

    @BeforeMethod
    public void createTestUser()
    {
        createUser( USERNAME, PASSWORD, NAME, "testuser@localhost", false );
    }

    @Test
    public void testEditAccount()
        throws Exception
    {
        doLogin( USERNAME, PASSWORD );

        selenium.click( "link=" + NAME );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assert selenium.isTextPresent( "Account Details" );

        selenium.type( "registerForm_user_fullName", "New User Name" );
        submit();

        assert selenium.isElementPresent( "link=New User Name" );
        assert !selenium.isElementPresent( "link=" + NAME );
    }

    @Test
    public void testEditAccountCancel()
    {
        doLogin( USERNAME, PASSWORD );

        selenium.click( "link=" + NAME );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assert selenium.isTextPresent( "Account Details" );

        selenium.type( "registerForm_user_fullName", "New User Name" );
        cancel();

        assertMainPage();

        assert !selenium.isElementPresent( "link=New User Name" );
        assert selenium.isElementPresent( "link=" + NAME );
    }

    @AfterMethod
    public void deleteUser()
    {
        deleteUser( USERNAME );
    }
}
