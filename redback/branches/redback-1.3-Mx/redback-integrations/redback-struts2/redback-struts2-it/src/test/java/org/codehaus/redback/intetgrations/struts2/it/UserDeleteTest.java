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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UserDeleteTest
    extends AbstractSeleniumTestCase
{
    public static final String USERNAME = "deleteuser";

    public static final String PASSWORD = "password1";

    @BeforeClass
    public void createUser()
    {
        createUser( USERNAME, PASSWORD, "Delete User", "deluser@localhost", true );
    }

    @Test
    public void deleteUser()
    {
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );

        selenium.open( "/security/userlist.action" );

        assert selenium.isElementPresent( "link=" + USERNAME );

        deleteUser( USERNAME );

        assert !selenium.isElementPresent( "link=" + USERNAME );
    }
}
