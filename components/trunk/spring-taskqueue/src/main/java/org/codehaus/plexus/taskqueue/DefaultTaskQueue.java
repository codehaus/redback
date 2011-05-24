package org.codehaus.plexus.taskqueue;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.redback.components.springutils.ComponentContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultTaskQueue
    implements TaskQueue
{

    private Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private ComponentContainer componentContainer;

    private List<String> taskEntryEvaluators;

    private List<TaskEntryEvaluator> taskEntryEvaluatorComponents = new ArrayList<TaskEntryEvaluator>();

    private List<String> taskExitEvaluators;

    private List<TaskExitEvaluator> taskExitEvaluatorComponents = new ArrayList<TaskExitEvaluator>();

    private List<String> taskViabilityEvaluators;

    private List<TaskViabilityEvaluator> taskViabilityEvaluatorComponents = new ArrayList<TaskViabilityEvaluator>();

    private BlockingQueue queue;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------


    @PostConstruct
    public void configure()
    {

        if ( taskEntryEvaluators != null )
        {

            for ( String taskEntryEvaluator : taskEntryEvaluators )
            {
                this.taskEntryEvaluatorComponents.add(
                    componentContainer.getComponent( TaskEntryEvaluator.class, taskEntryEvaluator ) );
            }

        }

        if ( taskExitEvaluators != null )
        {

            for ( String taskExitEvaluator : taskExitEvaluators )
            {
                this.taskExitEvaluatorComponents.add(
                    componentContainer.getComponent( TaskExitEvaluator.class, taskExitEvaluator ) );
            }
        }

        if ( taskViabilityEvaluators != null )
        {

            for ( String taskViabilityEvaluator : taskViabilityEvaluators )
            {
                this.taskViabilityEvaluatorComponents.add(
                    componentContainer.getComponent( TaskViabilityEvaluator.class, taskViabilityEvaluator ) );
            }

        }

        queue = new LinkedBlockingQueue();
    }

    // ----------------------------------------------------------------------
    // TaskQueue Implementation
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Queue operations
    // ----------------------------------------------------------------------

    public boolean put( Task task )
        throws TaskQueueException
    {
        // ----------------------------------------------------------------------
        // Check that all the task entry evaluators accepts the task
        // ----------------------------------------------------------------------

        for ( TaskEntryEvaluator taskEntryEvaluator : taskEntryEvaluatorComponents )
        {
            boolean result = taskEntryEvaluator.evaluate( task );

            if ( !result )
            {
                return false;
            }
        }

        // ----------------------------------------------------------------------
        // The task was accepted, enqueue it
        // ----------------------------------------------------------------------

        enqueue( task );

        // ----------------------------------------------------------------------
        // Check that all the task viability evaluators accepts the task
        // ----------------------------------------------------------------------

        for ( TaskViabilityEvaluator taskViabilityEvaluator : taskViabilityEvaluatorComponents )
        {
            Collection toBeRemoved = taskViabilityEvaluator.evaluate( Collections.unmodifiableCollection( queue ) );

            for ( Iterator it2 = toBeRemoved.iterator(); it2.hasNext(); )
            {
                Task t = (Task) it2.next();

                queue.remove( t );
            }
        }

        return true;
    }

    public Task take()
        throws TaskQueueException
    {
        while ( true )
        {
            Task task = dequeue();

            if ( task == null )
            {
                return null;
            }

            for ( TaskExitEvaluator taskExitEvaluator : taskExitEvaluatorComponents )
            {
                boolean result = taskExitEvaluator.evaluate( task );

                if ( !result )
                {
                    // the task wasn't accepted; drop it.
                    task = null;

                    break;
                }
            }

            if ( task != null )
            {
                return task;
            }
        }
    }

    public Task poll( int timeout, TimeUnit timeUnit )
        throws InterruptedException
    {
        return (Task) queue.poll( timeout, timeUnit );
    }

    public boolean remove( Task task )
        throws ClassCastException, NullPointerException
    {
        return queue.remove( task );
    }

    public boolean removeAll( List tasks )
        throws ClassCastException, NullPointerException
    {
        return queue.removeAll( tasks );
    }

    // ----------------------------------------------------------------------
    // Queue Inspection
    // ----------------------------------------------------------------------

    public List getQueueSnapshot()
        throws TaskQueueException
    {
        return Collections.unmodifiableList( new ArrayList( queue ) );
    }

    // ----------------------------------------------------------------------
    // Queue Management
    // ----------------------------------------------------------------------

    private void enqueue( Task task )
    {
        queue.offer( task );
    }

    private Task dequeue()
    {
        return (Task) queue.poll();
    }

    public List<String> getTaskEntryEvaluators()
    {
        return taskEntryEvaluators;
    }

    public void setTaskEntryEvaluators( List<String> taskEntryEvaluators )
    {
        this.taskEntryEvaluators = taskEntryEvaluators;
    }

    public List<String> getTaskExitEvaluators()
    {
        return taskExitEvaluators;
    }

    public void setTaskExitEvaluators( List<String> taskExitEvaluators )
    {
        this.taskExitEvaluators = taskExitEvaluators;
    }

    public List<String> getTaskViabilityEvaluators()
    {
        return taskViabilityEvaluators;
    }

    public void setTaskViabilityEvaluators( List<String> taskViabilityEvaluators )
    {
        this.taskViabilityEvaluators = taskViabilityEvaluators;
    }


    public List<TaskEntryEvaluator> getTaskEntryEvaluatorComponents()
    {
        return taskEntryEvaluatorComponents;
    }

    public void setTaskEntryEvaluatorComponents( List<TaskEntryEvaluator> taskEntryEvaluatorComponents )
    {
        this.taskEntryEvaluatorComponents = taskEntryEvaluatorComponents;
    }

    public List<TaskExitEvaluator> getTaskExitEvaluatorComponents()
    {
        return taskExitEvaluatorComponents;
    }

    public void setTaskExitEvaluatorComponents( List<TaskExitEvaluator> taskExitEvaluatorComponents )
    {
        this.taskExitEvaluatorComponents = taskExitEvaluatorComponents;
    }

    public List<TaskViabilityEvaluator> getTaskViabilityEvaluatorComponents()
    {
        return taskViabilityEvaluatorComponents;
    }

    public void setTaskViabilityEvaluatorComponents( List<TaskViabilityEvaluator> taskViabilityEvaluatorComponents )
    {
        this.taskViabilityEvaluatorComponents = taskViabilityEvaluatorComponents;
    }
}
