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

import org.testng.annotations.Test;

public class UserCreateTest
    extends AbstractSeleniumTestCase
{
    public static final String USERNAME = "newuser";

    public static final String PASSWORD = "newpass";

    @Test
    public void createUser()
    {
        createUser( USERNAME, PASSWORD, "New User", "newuser@localhost", true );

        selenium.open( "/security/userlist.action" );
        assert selenium.isElementPresent( "link=newuser" );
    }
}
