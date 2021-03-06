package org.codehaus.plexus.taskqueue.execution;

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

import junit.framework.TestCase;
import org.codehaus.plexus.taskqueue.BuildProjectTask;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml" } )
public class TaskQueueExecutorTest
    extends TestCase
{
    @Inject
    @Named( value = "taskQueue#default" )
    private TaskQueue taskQueue;

    // inject this to start the executor see @PostConstruct in {@link ThreadedTaskQueueExecutor
    @Inject
    @Named( value = "queueExecutor#default" )
    private TaskQueueExecutor taskQueueExecutor;


    @Test
    public void testTimeoutWithInterrupts()
        throws TaskQueueException, InterruptedException
    {
        BuildProjectTask task = putTask( 2 * 1000, false );

        waitForExpectedTaskEnd( task );

        assertTrue( task.isCancelled() );
        assertFalse( task.isDone() );
    }

    @Test
    public void testTimeoutWithoutInterrupts()
        throws TaskQueueException, InterruptedException
    {
        BuildProjectTask task = putTask( 2 * 1000, true );

        waitForExpectedTaskEnd( task );

        // the thread is killed so the task is neither done nor cancelled
        assertFalse( task.isCancelled() );
        assertFalse( task.isDone() );
    }

    private BuildProjectTask putTask( int executionTime, boolean ignoreInterrupts )
        throws TaskQueueException
    {
        BuildProjectTask task = new BuildProjectTask( 100 );
        task.setMaxExecutionTime( executionTime );
        task.setExecutionTime( 10 * executionTime );
        task.setIgnoreInterrupts( ignoreInterrupts );

        taskQueue.put( task );
        return task;
    }

    private static void waitForExpectedTaskEnd( BuildProjectTask task )
        throws InterruptedException
    {
        // thread scheduling may take some time, so we want to wait until the task
        // is actually running before starting to count the timeout.
        for ( int i = 0; i < 500; i++ )
        {
            if ( task.isStarted() )
            {
                break;
            }
            Thread.sleep( 10 );
        }

        assertTrue( "Task not started in 5 seconds - heavy load?", task.isStarted() );

        Thread.sleep( task.getMaxExecutionTime() );
    }
}
