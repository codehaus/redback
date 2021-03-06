package org.codehaus.plexus.cache.oscache;

/*
 * Copyright 2001-2007 The Codehaus.
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

import org.codehaus.plexus.cache.Cache;
import org.codehaus.plexus.cache.test.AbstractCacheTestCase;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Tests for OsCacheCache.
 *
 * @author <a href="mailto:Olivier.LAMY@accor.com">Olivier Lamy</a>
 * @version $Id$
 * @since 3 February, 2007
 */
public class OsCacheCacheTest
    extends AbstractCacheTestCase
{

    @Inject
    @Named( value = "cache#oscache" )
    Cache cache;

    @Inject
    @Named( value = "cache#alwaysrefresh" )
    Cache cachealwaysrefresh;

    @Inject
    @Named( value = "cache#onesecondrefresh" )
    Cache cacheonesecondrefresh;

    @Inject
    @Named( value = "cache#twosecondrefresh" )
    Cache cachetwosecondrefresh;

    @Inject
    @Named( value = "cache#neverrefresh" )
    Cache cacheneversecondrefresh;

    @Override
    public Cache getCache()
    {
        return cache;
    }

    public Cache getAlwaysRefresCache()
        throws Exception
    {
        return cachealwaysrefresh;
    }

    public Cache getNeverRefresCache()
        throws Exception
    {
        return cacheneversecondrefresh;
    }

    public Cache getOneSecondRefresCache()
        throws Exception
    {
        return cacheonesecondrefresh;
    }

    public Cache getTwoSecondRefresCache()
        throws Exception
    {
        return cachetwosecondrefresh;
    }

    public Class getCacheClass()
    {
        return OsCacheCache.class;
    }
}
