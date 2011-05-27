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

import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.impl.JobDetailImpl;

import java.util.Properties;

public interface Scheduler
{

    void scheduleJob( JobDetailImpl jobDetail, Trigger trigger )
        throws SchedulerException;

    /**
     * @param listener
     */
    void addGlobalJobListener( JobListener listener )
        throws SchedulerException;

    /**
     * @param listener
     */
    void addGlobalTriggerListener( TriggerListener listener )
        throws SchedulerException;

    void unscheduleJob( String jobName, String groupName )
        throws SchedulerException;

    boolean interruptSchedule( String jobName, String groupName )
        throws SchedulerException;

    void setProperties( Properties properties );

    Properties getProperties();

    void shutdown( boolean waitForJobsToComplete );

}
