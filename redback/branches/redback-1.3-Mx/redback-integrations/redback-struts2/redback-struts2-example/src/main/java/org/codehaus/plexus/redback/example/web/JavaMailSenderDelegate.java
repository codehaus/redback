package org.codehaus.plexus.redback.example.web;

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

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;

public class JavaMailSenderDelegate
    extends JavaMailSenderImpl
{
    private Boolean enabled;

    @Override
    public void send( MimeMessage mimeMessage )
        throws MailException
    {
        String host = getHost();
        if ( host == null )
        {
            host = getSession().getProperties().getProperty( "mail.smtp.host" );
        }

        // Special setting for host that allows us to disable in integration tests
        if ( !"disabled".equals( host ) )
        {
            super.send( mimeMessage );
        }
    }
}
