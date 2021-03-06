package org.codehaus.redback.intetgrations.struts2.it;

/*
 * Copyright 2011 The Codehaus.
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

/**
 * AbstractSeleniumTestCase
 */
public class AbstractSeleniumTestCase
{
    protected static final String PAGE_TIMEOUT = "30000";

    protected Selenium selenium;

    protected String baseUrl;

    protected static final String ADMIN_USERNAME = "admin";

    protected static final String ADMIN_PASSWORD = "admin1";

    @BeforeClass
    public void createSeleniumInstance()
    {
        baseUrl = "http://localhost:" + System.getProperty( "jetty.port", "8080" );
        selenium =
            new DefaultSelenium( "localhost",
                                 Integer.valueOf( System.getProperty( "selenium.server", "4444" ) ).intValue(),
                                 System.getProperty( "selenium.browser", "*firefox" ), baseUrl );
        selenium.start();
    }

    @BeforeSuite
    public void createAdminPage()
    {
        createSeleniumInstance();

        try
        {
            selenium.open( "/security/addadmin.action" );
            selenium.type( "adminCreateForm_user_fullName", "Admin User" );
            selenium.type( "adminCreateForm_user_email", "admin@localhost" );
            selenium.type( "adminCreateForm_user_password", "admin1" );
            selenium.type( "adminCreateForm_user_confirmPassword", "admin1" );
            selenium.click( "adminCreateForm_0" );
            selenium.waitForPageToLoad( PAGE_TIMEOUT );
        }
        finally
        {
            shutdownSelenium();
        }
    }

    protected void doLogin( String username, String password )
    {
        selenium.deleteAllVisibleCookies();
        selenium.open( "/security/login.action" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "loginForm_username", username );
        selenium.type( "loginForm_password", password );
        selenium.click( "loginForm__login" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    @AfterClass
    public void shutdownSelenium()
    {
        selenium.stop();
    }
}
