package org.codehaus.plexus.redback.xwork.mail;

/*
* Copyright 2005-2006 The Codehaus.
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

import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManager;
import org.codehaus.plexus.redback.keys.KeyManagerException;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;

/**
 * Test the Mailer class.
 */
public class MailGeneratorTest
    extends PlexusInSpringTestCase
{
    private MailGenerator generator;

    private UserSecurityPolicy policy;

    private KeyManager keyManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        generator = (MailGenerator) lookup( MailGenerator.ROLE, "velocity" );

        policy = (UserSecurityPolicy) lookup( UserSecurityPolicy.ROLE );

        keyManager = (KeyManager) lookup( KeyManager.ROLE, "memory" );
    }

    public void testGeneratePasswordResetMail()
        throws KeyManagerException
    {
        AuthenticationKey authkey = keyManager.createKey( "username", "Password Reset Request",
                                                          policy.getUserValidationSettings().getEmailValidationTimeout() );

        String content = generator.generateMail( "passwordResetEmail", authkey, "baseUrl" );

        assertNotNull( content );
        assertTrue( content.indexOf( '$' ) == -1 ); // make sure everything is properly populate
    }

    public void testGenerateAccountValidationMail()
        throws KeyManagerException
    {
        AuthenticationKey authkey = keyManager.createKey( "username", "New User Email Validation",
                                                          policy.getUserValidationSettings().getEmailValidationTimeout() );

        String content = generator.generateMail( "newAccountValidationEmail", authkey, "baseUrl" );

        assertNotNull( content );
        assertTrue( content.indexOf( '$' ) == -1 ); // make sure everything is properly populate
    }

    public void testGenerateAccountValidationMailCustomUrl()
        throws Exception
    {
        AuthenticationKey authkey = keyManager.createKey( "username", "New User Email Validation",
                                                          policy.getUserValidationSettings().getEmailValidationTimeout() );

        generator = (MailGenerator) lookup( MailGenerator.ROLE, "custom-url" );
        String content = generator.generateMail( "newAccountValidationEmail", authkey, "baseUrl" );

        assertNotNull( content );
        assertTrue( content.indexOf( "baseUrl" ) == -1 ); // make sure everything is properly populate
        assertTrue( content.indexOf( "MY_APPLICATION_URL/security" ) > 0 ); // make sure everything is properly populate
    }

    public void testGeneratePasswordResetMailCustomUrl()
        throws Exception
    {
        AuthenticationKey authkey = keyManager.createKey( "username", "Password Reset Request",
                                                          policy.getUserValidationSettings().getEmailValidationTimeout() );

        generator = (MailGenerator) lookup( MailGenerator.ROLE, "custom-url" );
        String content = generator.generateMail( "passwordResetEmail", authkey, "baseUrl" );

        assertNotNull( content );
        assertTrue( content.indexOf( "baseUrl" ) == -1 ); // make sure everything is properly populate
        assertTrue( content.indexOf( "MY_APPLICATION_URL/security" ) > 0 ); // make sure everything is properly populate
    }
}
