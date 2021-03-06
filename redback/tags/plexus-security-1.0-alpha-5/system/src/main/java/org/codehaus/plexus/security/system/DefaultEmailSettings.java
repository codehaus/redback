package org.codehaus.plexus.security.system;

/*
 * Copyright 2001-2006 The Codehaus.
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

/**
 * DefaultEmailSettings 
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * 
 * @plexus.component role="org.codehaus.plexus.security.system.EmailSettings"
 */
public class DefaultEmailSettings
    implements EmailSettings
{

    /**
     * @plexus.configuration default-value="/feedback.action"
     */
    private String feedback;

    /**
     * @plexus.configuration default-value="security@unconfigured.com"
     */
    private String fromAddress;

    /**
     * @plexus.configuration default-value="Unconfigured Usernamed"
     */
    private String fromUsername;

    public String getFeedback()
    {
        return feedback;
    }

    public String getFromAddress()
    {
        return fromAddress;
    }

    public String getFromUsername()
    {
        return fromUsername;
    }

}
