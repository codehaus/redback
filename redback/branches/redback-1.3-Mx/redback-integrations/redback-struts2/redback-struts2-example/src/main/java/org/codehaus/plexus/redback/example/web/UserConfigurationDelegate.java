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

import org.codehaus.plexus.redback.configuration.UserConfiguration;

import java.util.HashMap;
import java.util.Map;

public class UserConfigurationDelegate
    extends UserConfiguration
{
    private Map<String, String> overrides = new HashMap<String, String>();

    @Override
    public boolean getBoolean( String key )
    {
        return overrides.containsKey( key ) ? Boolean.valueOf( overrides.get( key ) ) : super.getBoolean( key );
    }

    public void addOverride( String key, String value )
    {
        overrides.put( key, value );
    }
}
