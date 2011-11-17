package org.codehaus.redback.rest.services.interceptors;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * @author Olivier Lamy
 * @since 1.4
 */
@Service( "httpContextThreadLocalInterceptor#redback" )
public class HttpContextThreadLocalInterceptor
    implements RequestHandler
{
    public Response handleRequest( Message m, ClassResourceInfo resourceClass )
    {
        HttpContextThreadLocal.set( new HttpContext().setHttpServletRequest( getHttpServletRequest( m ) ) );
        return null;
    }

    public HttpServletRequest getHttpServletRequest( Message message )
    {
        // FIXME use a constant from cxf
        return (HttpServletRequest) message.get( "HTTP.REQUEST" );
    }
}
