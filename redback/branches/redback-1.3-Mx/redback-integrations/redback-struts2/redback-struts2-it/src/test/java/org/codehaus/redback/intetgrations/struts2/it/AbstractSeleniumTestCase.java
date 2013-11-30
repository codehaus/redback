package org.codehaus.redback.intetgrations.struts2.it;

/*
 * Copyright 2009 The Codehaus.
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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Note: dependencies are for interdependent tests (as in, won't pass if this doesn't), not for sequencing the UI.
 * Always ensure that the test adequately prepares its own state
 */
public abstract class AbstractSeleniumTestCase
{
    protected static final String PAGE_TIMEOUT = "30000";

    protected static final String ADMIN_USERNAME = "admin";

    protected static final String ADMIN_PASSWORD = "admin1";

    protected static Selenium selenium;

    protected static String baseUrl;

    protected void doLogin( String username, String password )
    {
        selenium.deleteAllVisibleCookies();
        selenium.open( "/security/login.action" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "loginForm_username", username );
        selenium.type( "loginForm_password", password );
        selenium.click( "//input[@value='Login']" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    @BeforeSuite
    public void createSeleniumInstance()
    {
        baseUrl = System.getProperty( "baseUrl", "http://localhost:9595/" );
        selenium = new DefaultSelenium( "localhost", Integer.valueOf( System.getProperty( "selenium.server", "4444" ) ),
                                        System.getProperty( "selenium.browser", "*firefox" ), baseUrl );
        selenium.start();
    }

    @BeforeSuite( dependsOnMethods = "createSeleniumInstance" )
    public void createAdminPage()
    {
        selenium.open( "/" );

        if ( selenium.isTextPresent( "Create Admin User" ) )
        {
            selenium.type( "adminCreateForm_user_fullName", "Admin User" );
            selenium.type( "adminCreateForm_user_email", "admin@localhost" );
            selenium.type( "adminCreateForm_user_password", ADMIN_PASSWORD );
            selenium.type( "adminCreateForm_user_confirmPassword", ADMIN_PASSWORD );
            selenium.click( "adminCreateForm_0" );
            selenium.waitForPageToLoad( PAGE_TIMEOUT );
        }
    }

    @AfterSuite
    public void shutdownSelenium()
    {
        selenium.stop();
    }

    protected void createUser( String username, String password, String name, String email,
                               boolean forcePasswordChange )
    {
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );

        selenium.open( "/security/userlist.action" );

        if ( !selenium.isElementPresent( "link=" + username ) )
        {
            createUserNoRoles( username, password, name, email );

            selenium.click( "addRolesToUser_submitRolesButton" );
            selenium.waitForPageToLoad( PAGE_TIMEOUT );

            if ( !forcePasswordChange )
            {
                selenium.click( "link=" + username );
                selenium.waitForPageToLoad( PAGE_TIMEOUT );
                selenium.uncheck( "userEditForm_user_passwordChangeRequired" );
                submit();

                confirmAdminPassword();
            }
        }
    }

    protected void createUserNoRoles( String username, String password, String name, String email )
    {
        selenium.click( "usercreate_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "userCreateForm_user_username", username );
        selenium.type( "userCreateForm_user_fullName", name );
        selenium.type( "userCreateForm_user_email", email );
        selenium.type( "userCreateForm_user_password", password );
        selenium.type( "userCreateForm_user_confirmPassword", password );
        selenium.click( "userCreateForm_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    protected void submit()
    {
        selenium.click( "//input[@type='submit']" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    protected void cancel()
    {
        selenium.click( "//input[@name='cancel']" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    protected void deleteUserIfExists( String username )
    {
        deleteUser( username, false );
    }

    protected void deleteUser( String username )
    {
        deleteUser( username, true );
    }

    private void deleteUser( String username, boolean failIfMissing )
    {
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );

        selenium.open( "/security/userlist.action" );
        if ( failIfMissing || selenium.isElementPresent( "link=" + username ) )
        {
            selenium.click( "//tr[.//a[text()='" + username + "']]/td/a[@title='delete user']" );
            selenium.waitForPageToLoad( PAGE_TIMEOUT );

            assert selenium.isTextPresent( "[Admin] User Delete" );

            submit();
        }
    }

    protected void confirmAdminPassword()
    {
        selenium.type( "userEditForm_userAdminPassword", ADMIN_PASSWORD );
        selenium.click( "userEditForm_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    protected void assertMainPage()
    {
        assert selenium.isTextPresent( "This is the example mainpage" );
    }

    protected void createUser( String username, String password, String name, String email )
    {
        createUser( username, password, name, email, true );
    }
}
