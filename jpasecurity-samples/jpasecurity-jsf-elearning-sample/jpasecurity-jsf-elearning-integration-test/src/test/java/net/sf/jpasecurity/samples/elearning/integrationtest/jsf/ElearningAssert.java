/*
 * Copyright 2011 Raffaela Ferrari open knowledge GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.sf.jpasecurity.samples.elearning.integrationtest.jsf;

import static org.junit.Assert.assertEquals;
import net.sf.jpasecurity.samples.elearning.integrationtest.jsf.AbstractHtmlTestCase.Role;

import org.jaxen.JaxenException;
import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Raffaela Ferrari
 */
public class ElearningAssert {

    public static void assertIndexPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        if (role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//a[@href = 'courses.xhtml'][text() = 'Courses']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'teachers.xhtml'][text() = 'Teachers']").size());
    }

    public static void assertCoursesPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Available courses']").size());
        if (role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=1'][text() = 'Shakespeare course']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=2'][text() = 'Da Vinci course']").size());
    }

    public static void assertCoursePage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        switch (role) {
            case TEACHER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'button'][@value = 'Create new lesson']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'leave this course']").size());
                assertEquals(1, page.getByXPath("//h2[text() = 'Participants']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=2'][text() = 'Stefan A.']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=4'][text() = 'Tassimo B.']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=5'][text() = 'Ulli D.']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'student.xhtml?id=6'][text() = 'Anne G.']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'student.xhtml?id=7'][text() = 'Lisa T.']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=8'][text() = 'Marie M.']").size());
                break;
            case STUDENT:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'button'][@value = 'Create new lesson']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'leave this course']").size());
                assertEquals(1, page.getByXPath("//h2[text() = 'Participants']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=2'][text() = 'Stefan A.']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=4'][text() = 'Tassimo B.']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=5'][text() = 'Ulli D.']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'student.xhtml?id=6'][text() = 'Anne G.']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'student.xhtml?id=7'][text() = 'Lisa T.']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=8'][text() = 'Marie M.']").size());
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'join this course']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'leave this course']").size());
                break;
            default:
                Assert.fail();
                break;
        }
        assertEquals(1, page.getByXPath("//h1[text() = 'Analysis']").size());
        assertEquals(1, page.getByXPath("//h2[text() = 'Lecturer']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'teacher.xhtml?teacher=1'][text() = 'Peter B.']").size());
        assertEquals(1, page.getByXPath("//h2[text() = 'Lessons']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'lesson.xhtml?course=3&lesson=0']"
                + "[text() = 'Analysis introduction']").size());
    }

    public static void assertDashboardPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        switch (role) {
            case TEACHER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                assertEquals(1, page.getByXPath("//h1[text() = 'Create new course']").size());
                assertEquals(1, page.getByXPath("//h1[text() = 'My courses']").size());
                assertEquals(0, page.getByXPath("//h1[text() = 'Available courses']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'create']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=1']"
                        + "[text() = 'Shakespeare course']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'course.xhtml?course=2']"
                        + "[text() = 'Da Vinci course']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=3'][text() = 'Analysis']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'course.xhtml?course=4'][text() = 'Algebra']").size());
                break;
            case STUDENT:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                assertEquals(0, page.getByXPath("//h1[text() = 'Create new course']").size());
                assertEquals(1, page.getByXPath("//h1[text() = 'My courses']").size());
                assertEquals(1, page.getByXPath("//h1[text() = 'Available courses']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'create']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=1']"
                        + "[text() = 'Shakespeare course']").size());
                assertEquals(2, page.getByXPath("//a[@href = 'course.xhtml?course=2']"
                        + "[text() = 'Da Vinci course']").size());
                assertEquals(2, page.getByXPath("//a[@href = 'course.xhtml?course=3'][text() = 'Analysis']").size());
                assertEquals(2, page.getByXPath("//a[@href = 'course.xhtml?course=4'][text() = 'Algebra']").size());
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Username: ']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Login']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@value = 'Cancel']").size());
                break;
            default:
                Assert.fail();
                break;
        }
    }

    public static void assertLessonCreatorPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        switch(role) {
            case TEACHER:
                assertEquals(1, page.getByXPath("//h1[text() = 'Create new lesson']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Course title:']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Lesson title:']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Content:']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'cancel']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'create new lesson']").size());
                break;
            case STUDENT:
                assertEquals(1, page.getByXPath("//h1[text() = 'HTTP Status 403 "
                        + "- Access to the requested resource has been denied']").size());
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Username: ']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Login']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@value = 'Cancel']").size());
                break;
            default:
                Assert.fail();
                break;
        }
    }

    public static void assertLessonPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        switch (role) {
            case TEACHER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'start this lesson']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'resolve this lesson']").size());
                assertEquals(0, page.getByXPath("//div[text() = 'Lesson is resolved']").size());
                break;
            case STUDENT:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'start this lesson']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'resolve this lesson']").size());
                assertEquals(0, page.getByXPath("//div[text() = 'Lesson is resolved']").size());
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'start this lesson']").size());
                assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'resolve this lesson']").size());
                assertEquals(0, page.getByXPath("//div[text() = 'Lesson is resolved']").size());
                break;
            default:
                Assert.fail();
                break;
        }
        assertEquals(1, page.getByXPath("//h2[text() = 'Content']").size());
        assertEquals(1, page.getByXPath("//div[contains(., 'Welcome to the Analysis course.')]").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Course:']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Lecturer:']").size());
    }

    public static void assertLoginPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        if (role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//label[text() = 'Username: ']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Password: ']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Login']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Cancel']").size());
    }

    public static void assertStudentPage(HtmlPage page, Role role) throws JaxenException {
        switch(role) {
            case TEACHER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                assertEquals("E-Learning Platform", page.getTitleText());
                assertEquals(1, page.getByXPath("//h1[text() = 'Marie M.']").size());
                assertEquals(1, page.getByXPath("//h2[text() = 'Selected Courses']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'course.xhtml?course=1']"
                        + "[text() = 'Shakespeare course']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'course.xhtml?course=2']"
                        + "[text() = 'Da Vinci course']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=3'][text() = 'Analysis']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'course.xhtml?course=4'][text() = 'Algebra']").size());
                break;
            case STUDENT:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                assertEquals("E-Learning Platform", page.getTitleText());
                assertEquals(1, page.getByXPath("//h1[text() = 'Marie M.']").size());
                assertEquals(1, page.getByXPath("//h2[text() = 'Selected Courses']").size());
                assertEquals(0, page.getByXPath("//a[@href = 'course.xhtml?course=1']"
                        + "[text() = 'Shakespeare course']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=2']"
                        + "[text() = 'Da Vinci course']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=3'][text() = 'Analysis']").size());
                assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=4'][text() = 'Algebra']").size());
                break;
            case GUEST:
                assertEquals("E-Learning Platform", page.getTitleText());
                assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Username: ']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Login']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Cancel']").size());
                break;
            default:
                Assert.fail();
                break;
        }
    }

    public static void assertTeacherPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Peter B.']").size());
        if (role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//h2[text() = 'Lectured Courses']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=1'][text() = 'Shakespeare course']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?course=3'][text() = 'Analysis']").size());
    }

    public static void assertTeachersPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Teachers']").size());
        if (role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//a[@href = 'teacher.xhtml?teacher=1'][text() = 'Peter B.']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'teacher.xhtml?teacher=3'][text() = 'Hans L.']").size());
    }
}
