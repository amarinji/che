/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.console;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.gwtmockito.GwtMockitoTestRunner;

/**
 * JUnit test for Stacktrace and Compilation Error/Message line detection in
 * CompoundOutputCustomizer that is constructed with: 
 * - JavaOutputCustomizer - that is expected to process Java Stacktrace lines 
 * - CSharpOutputCustomizer - that is expected to process C# Compilation 
 *   Error/Warning and Stacktrace lines
 * 
 * See: CHE-15 - Java stacktrace support (From Platform to Che Workspace) See:
 * Issue #5489 - .NET C# stacktrace support #5489
 * 
 * @author Victor Rubezhny
 */
@RunWith(GwtMockitoTestRunner.class)
public class CompoundOutputCustomizerTest extends BaseOutputCustomizerTest {
    @Mock
    AppContext appContext;
    @Mock
    EditorAgent editorAgent;

    @Before
    public void setUp() throws Exception {
        OutputCustomizer[] customizers = new OutputCustomizer[] { 
                new JavaOutputCustomizer(appContext, editorAgent),
                new CSharpOutputCustomizer(appContext, editorAgent)
            };

        setupTestCustomizers(new CompoundOutputCustomizer(customizers), customizers);
    }

    /**
     * Test for the detection of initial stacktrace lines in
     * CompoundOutputCustomizer. These lines are not to be customized, however these
     * lines show an examples of beginning the StackTrace and might be used in
     * future to set up the customizer properly.
     * 
     * @throws Exception
     */
    @Test
    public void testInitialStackTraceLines() throws Exception {
        // Java Stacktrace lines
        testStackTraceLine("org.test.HighLevelException: org.test.MidLevelException: org.test.LowLevelException");
        testStackTraceLine("Caused by: org.test.MidLevelException: org.test.LowLevelException");
        testStackTraceLine("Caused by: org.test.LowLevelException");
        testStackTraceLine("Exception in thread \"main\" java.lang.ArithmeticException: / by zero");

        // .NET C# Stacktrace lines
        testStackTraceLine(
                "Unhandled Exception: System.NullReferenceException: Object reference not set to an instance of an object.");
    }

    /**
     * Test for the detection of informative stacktrace lines in
     * CompoundOutputCustomizer. These lines have an information on qualified path,
     * file name and line number for an exception
     * 
     * @throws Exception
     */
    @Test
    public void testValuableStackTraceLines() throws Exception {
        // Java Stacktrace lines
        testStackTraceLine(JavaOutputCustomizer.class, 
                "   at org.test.Junk.main(Junk.java:6)",
                "   at org.test.Junk.main(<a href='javascript:open(\"org.test.Junk.main\", \"Junk.java\", 6);'>Junk.java:6</a>)");
        testStackTraceLine(JavaOutputCustomizer.class, 
                "   at org.test.TrashClass.throwItThere(Junk.java:51)",
                "   at org.test.TrashClass.throwItThere(<a href='javascript:open(\"org.test.TrashClass.throwItThere\", \"Junk.java\", 51);'>Junk.java:51</a>)");
        testStackTraceLine(JavaOutputCustomizer.class, 
                "   at MyClass$ThrowInConstructor.<init>(MyClass.java:16)",
                "   at MyClass$ThrowInConstructor.<init>(<a href='javascript:open(\"MyClass$ThrowInConstructor.<init>\", \"MyClass.java\", 16);'>MyClass.java:16</a>)");

        // .NET C# Stacktrace lines
        testStackTraceLine(CSharpOutputCustomizer.class,
                "   at hwapp.ppp.PPPProgram.Main1(String[] args) in /home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18",
                "   at hwapp.ppp.PPPProgram.Main1(String[] args) in <a href='javascript:openCSSTL(\"/home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs\",18);'>/home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18</a>");
        testStackTraceLine(CSharpOutputCustomizer.class,
                "   at hwapp.Program.Main(String[] args) in /home/jeremy/projects/csharp/hwapp/Program.cs:line 10",
                "   at hwapp.Program.Main(String[] args) in <a href='javascript:openCSSTL(\"/home/jeremy/projects/csharp/hwapp/Program.cs\",10);'>/home/jeremy/projects/csharp/hwapp/Program.cs:line 10</a>");

        // .NET C# Compilation Error/Warning message lines
        testStackTraceLine(CSharpOutputCustomizer.class,
                "Program.cs(2,13): error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
                "<a href='javascript:openCSCM(\"Program.cs\",\"/home/jeremy/projects/csharp/hwapp/hwapp.csproj\",2,13);'>Program.cs(2,13)</a>: error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");
        testStackTraceLine(CSharpOutputCustomizer.class,
                "ppp/PPPProgram.cs(9,17): warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
                "<a href='javascript:openCSCM(\"ppp/PPPProgram.cs\",\"/home/jeremy/projects/csharp/hwapp/hwapp.csproj\",9,17);'>ppp/PPPProgram.cs(9,17)</a>: warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");
    }

    /**
     * Test for the detection of other stacktrace lines in CompoundOutputCustomizer.
     * Other lines that can be a part of Stack Trace, however do not contain any
     * useful information
     * 
     * @throws Exception
     */
    @Test
    public void testOtherStackTraceLines() throws Exception {
        // Java Stacktrace lines
        testStackTraceLine("   ... 1 more");
    }

    /**
     * Test for the detection of non-stacktrace lines in CompoundOutputCustomizer.
     * Other lines that might occur in output console
     * 
     * @throws Exception
     */
    @Test
    public void testNonStackTraceLines() throws Exception {
        testStackTraceLine("[STDOUT] Listening for transport dt_socket at address: 4403");
    }
}
