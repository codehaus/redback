package org.codehaus.plexus.redback.example.web.action;

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

import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.codehaus.plexus.redback.example.web.UserConfigurationDelegate;
import org.codehaus.plexus.redback.policy.DefaultUserValidationSettings;
import org.codehaus.plexus.redback.policy.UserValidationSettings;
import org.codehaus.plexus.redback.struts2.action.RedbackActionSupport;
import org.codehaus.plexus.registry.Registry;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;

/**
 * Reconfiguation action - just used for integration testing different scenarios, should not be something included in a
 * normal application.
 */
@Controller( "reconfig" )
@Scope( "prototype" )
public class ReconfigAction
    extends RedbackActionSupport
{
    private String name;

    private String value;

    @Inject
    private UserValidationSettings userValidationSettings;

    @Inject
    private UserConfiguration userConfiguration;

    public String execute()
    {
        UserConfigurationDelegate delegate = (UserConfigurationDelegate) userConfiguration;
        delegate.addOverride( name, value );

        // prod certain objects to grab config
        ((DefaultUserValidationSettings)userValidationSettings).initialize();

        return SUCCESS;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
    }
}
