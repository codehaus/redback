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

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.backportconcurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml" } )
public class TaskQueueTest
    extends TestCase
{
    @Inject
    @Named( value = "taskQueue#taskQueueTest" )
    private TaskQueue taskQueue;


    // NOTE: If we were using a blocking queue, the sleep/continue in the ThreadedTaskQueueExecutor wouldn't
    // be necessary; the queue would block until an element was available.
    @Test
    public void testEmptyQueue()
        throws Exception
    {
        assertNull( taskQueue.take() );
    }

    @Test
    public void testTaskEntryAndExitEvaluators()
        throws Exception
    {
        assertTaskIsAccepted( new BuildProjectTask( true, true, true, true ) );

        assertTaskIsRejected( new BuildProjectTask( false, true, true, true ) );

        assertTaskIsRejected( new BuildProjectTask( true, false, true, true ) );

        assertTaskIsRejected( new BuildProjectTask( true, true, false, true ) );

        assertTaskIsRejected( new BuildProjectTask( true, true, true, false ) );
    }

    @Test
    public void testTaskViabilityEvaluators()
        throws Exception
    {
        // The first and last task should be accepted

        Task task1 = new BuildProjectTask( 0 );

        Task task2 = new BuildProjectTask( 10 );

        Task task3 = new BuildProjectTask( 20 );

        Task task4 = new BuildProjectTask( 30 );

        Task task5 = new BuildProjectTask( 40 );

        Task task6 = new BuildProjectTask( 100 );

        assertTrue( taskQueue.put( task1 ) );

        assertTrue( taskQueue.put( task2 ) );

        assertTrue( taskQueue.put( task3 ) );

        assertTrue( taskQueue.put( task4 ) );

        assertTrue( taskQueue.put( task5 ) );

        assertTrue( taskQueue.put( task6 ) );

        Task actualTask1 = taskQueue.take();

        assertNotNull( actualTask1 );

        assertEquals( task1, actualTask1 );

        Task actualTask6 = taskQueue.take();

        assertNotNull( actualTask6 );

        assertEquals( task6, actualTask6 );

        assertNull( taskQueue.take() );
    }

    @Test
    public void testRemoveTask()
        throws Exception
    {
        Task task = new BuildProjectTask( 0 );

        taskQueue.put( task );

        taskQueue.remove( task );

        assertNull( taskQueue.take() );
    }

    @Test
    public void testRemoveAll()
        throws Exception
    {

        BlockingQueue<String> foo = new LinkedBlockingQueue<String>();
        foo.offer("1");
        foo.offer("2");

        Task firstTask = new BuildProjectTask( 110 );

        taskQueue.put( firstTask );

        Task secondTask = new BuildProjectTask( 11120 );

        taskQueue.put( secondTask );

        assertEquals( 2, taskQueue.getQueueSnapshot().size() );

        List tasks = new ArrayList();

        tasks.add( firstTask );

        tasks.add( secondTask );

        taskQueue.removeAll( tasks );

        assertTrue( taskQueue.getQueueSnapshot().isEmpty() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void assertTaskIsAccepted( Task expectedTask )
        throws Exception
    {
        taskQueue.put( expectedTask );

        Task actualTask = taskQueue.take();

        assertEquals( expectedTask, actualTask );
    }

    private void assertTaskIsRejected( Task expectedTask )
        throws Exception
    {
        taskQueue.put( expectedTask );

        Task actualTask = taskQueue.take();

        assertNull( actualTask );
    }
}
