package org.codehaus.redback.components.scheduler;

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

import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Matcher;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.utils.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

public class DefaultScheduler
    implements Scheduler
{

    private Logger log = LoggerFactory.getLogger( getClass() );

    private Properties properties;

    private StdScheduler scheduler;

    public void scheduleJob( JobDetailImpl jobDetail, Trigger trigger )
        throws SchedulerException
    {
        if ( jobDetail == null || jobDetail.getName() == null )
        {
            throw new SchedulerException( "No job or no job name - cannot schedule this job" );
        }

        if ( jobExists( jobDetail.getName(), jobDetail.getGroup() ) )
        {
            log.warn( "Will not schedule this job as a job {" + jobDetail.getName() + ":" + jobDetail.getGroup()
                          + "} already exists." );

            return;
        }

        try
        {
            scheduler.scheduleJob( jobDetail, trigger );
        }
        catch ( SchedulerException e )
        {
            throw new SchedulerException( "Error scheduling job.", e );
        }
        catch ( Exception e )
        {
            throw new SchedulerException( "Error scheduling job (Verify your cron expression).", e );
        }
    }

    public void addGlobalJobListener( JobListener listener )
        throws SchedulerException
    {
        scheduler.getListenerManager().addJobListener( listener, new AllMatch() );
    }

    public void addGlobalTriggerListener( TriggerListener listener )
        throws SchedulerException
    {
        scheduler.getListenerManager().addTriggerListener( listener, new AllMatch() );
    }

    private static class AllMatch<T extends Key> implements Matcher<T>
    {
        public boolean isMatch( T key )
        {
            return true;
        }
    }

    @PostConstruct
    public void initialize()
        throws SchedulerException
    {
        SchedulerFactory factory = new StdSchedulerFactory( properties );

        scheduler = (StdScheduler) factory.getScheduler();

        scheduler.start();

    }

    @PreDestroy
    public void stop()
    {
        scheduler.shutdown();
    }

    public void unscheduleJob( String jobName, String groupName )
        throws SchedulerException
    {
        if ( jobName == null )
        {
            throw new SchedulerException( "Job name null - cannot unschedule job" );
        }

        try
        {
            if ( jobExists( jobName, groupName ) )
            {
                scheduler.deleteJob( new JobKey( jobName, groupName ) );
            }
        }
        catch ( SchedulerException e )
        {
            throw new SchedulerException( "Error unscheduling job.", e );
        }
    }

    public boolean interruptSchedule( String jobName, String groupName )
        throws SchedulerException
    {
        try
        {
            return scheduler.interrupt( new JobKey( jobName, groupName ) );
        }
        catch ( Exception e )
        {
            throw new SchedulerException( "Can't interrup job \"" + jobName + "\".", e );
        }
    }

    private boolean jobExists( String jobName, String jobGroup )
        throws SchedulerException
    {

        return ( scheduler.getJobDetail( new JobKey( jobName, jobGroup ) ) != null );
    }

    public void shutdown( boolean waitForJobsToComplete )
    {
        log.info( "call shutdown waitForJobsToComplete : {}", waitForJobsToComplete );
        scheduler.shutdown( waitForJobsToComplete );
    }

    public StdScheduler getScheduler()
    {
        return scheduler;
    }

    public void setProperties( Properties properties )
    {
        this.properties = properties;
    }

    public Properties getProperties()
    {
        return properties;
    }
}
