/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.plexus.spring.editors;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.propertyeditors.CustomMapEditor;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 *
 * @version $Id$
 */
public class MapPropertyEditor
    extends CustomMapEditor
    implements PropertyEditorRegistrar
{

    private Class<?> type, implementation;
    
    public MapPropertyEditor( Class type, Class implementation )
    {
        super( type );
        this.type = type;
        this.implementation = implementation;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.PropertyEditorRegistrar#registerCustomEditors(org.springframework.beans.PropertyEditorRegistry)
     */
    public void registerCustomEditors( PropertyEditorRegistry registry )
    {
        registry.registerCustomEditor( type, this );
    }
    
    /**
     * @see org.springframework.beans.propertyeditors.PropertiesEditor#setAsText(java.lang.String)
     */
    public void setAsText( String text )
        throws IllegalArgumentException
    {
        if ( StringUtils.isBlank( text ) )
        {
            setValue( null );
            return;
        }

        Map c = (Map) BeanUtils.instantiateClass( implementation );

        try
        {
            SAXReader reader = new SAXReader();
            Document doc = reader.read( new StringReader( text ) );
            Element root = doc.getRootElement();
            for ( Iterator i = root.elementIterator(); i.hasNext(); )
            {
                Element element = (Element) i.next();
                String key = element.getName();
                String value = element.getTextTrim();
                c.put( key, value );
            }
            setValue( c );
        }
        catch ( DocumentException e )
        {
        }
}    

}
