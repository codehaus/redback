package org.codehaus.plexus.redback.struts2.interceptor;

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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.redback.system.check.EnvironmentCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * EnvironmentCheckInterceptor
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.interceptor.Interceptor"
 * role-hint="redbackEnvironmentCheckInterceptor"
 */
public class EnvironmentCheckInterceptor
    implements Interceptor
{
    private static boolean checked = false;

    private Logger log = LoggerFactory.getLogger( EnvironmentCheckInterceptor.class );

    /**
     * @plexus.requirement role="org.codehaus.plexus.redback.system.check.EnvironmentCheck"
     */
    private List<EnvironmentCheck> checkers;

    public void destroy()
    {
        // no-op
    }

    public void init()
    {
        if ( EnvironmentCheckInterceptor.checked )
        {
            // No need to check twice.
            return;
        }

        if ( checkers != null )
        {
            List<String> violations = new ArrayList<String>();

            for ( EnvironmentCheck check : checkers )
            {
                check.validateEnvironment( violations );
            }

            if ( !violations.isEmpty() )
            {
                StringBuffer msg = new StringBuffer();
                msg.append( "EnvironmentCheck Failure.\n" );
                msg.append( "======================================================================\n" );
                msg.append( " ENVIRONMENT FAILURE !! \n" );
                msg.append( "\n" );

                for ( String v : violations )
                {
                    msg.append( v ).append( "\n" );
                }

                msg.append( "\n" );
                msg.append( "======================================================================" );
                log.error( msg.toString() );
            }
        }

        EnvironmentCheckInterceptor.checked = true;
    }

    public String intercept( ActionInvocation invocation )
        throws Exception
    {
        // A no-op here. Work for this intereceptor is done in init().
        return invocation.invoke();
    }
}
