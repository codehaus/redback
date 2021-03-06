package org.codehaus.plexus.redback.policy;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
 * MustChangePasswordException 
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class MustChangePasswordException
    extends PolicyViolationException
{
    public MustChangePasswordException()
    {
        super();
    }

    public MustChangePasswordException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public MustChangePasswordException( String message )
    {
        super( message );
    }

    public MustChangePasswordException( Throwable cause )
    {
        super( cause );
    }
}
