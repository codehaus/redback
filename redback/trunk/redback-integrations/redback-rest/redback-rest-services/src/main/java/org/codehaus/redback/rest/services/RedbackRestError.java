package org.codehaus.redback.rest.services;
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

import org.codehaus.redback.rest.api.services.RedbackServiceException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Olivier Lamy
 * @since 1.4
 */
@XmlRootElement( name = "redbackRestError" )
public class RedbackRestError
{

    private int httpErrorCode;

    private List<String> errorKeys = new ArrayList<String>();

    private String errorMessage;

    public RedbackRestError()
    {
        // no op
    }

    public RedbackRestError( RedbackServiceException e )
    {
        httpErrorCode = e.getHttpErrorCode();
        errorKeys.add( e.getErrorKey() );
        errorMessage = e.getMessage();
    }

    public int getHttpErrorCode()
    {
        return httpErrorCode;
    }

    public void setHttpErrorCode( int httpErrorCode )
    {
        this.httpErrorCode = httpErrorCode;
    }


    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage( String errorMessage )
    {
        this.errorMessage = errorMessage;
    }

    public List<String> getErrorKeys()
    {
        return errorKeys;
    }

    public void setErrorKeys( List<String> errorKeys )
    {
        this.errorKeys = errorKeys;
    }

}