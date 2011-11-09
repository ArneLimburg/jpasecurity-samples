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
package net.sf.jpasecurity.samples.elearning.jsf.presentation;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.LessonWithoutCourse;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.Title;
import net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate;
import net.sf.jpasecurity.sample.elearning.domain.course.LessonFactoryBuilder;
import net.sf.jpasecurity.samples.elearning.jsf.service.ElearningRepository;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService.Callable;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "course")
public class CourseBean {

    @ManagedProperty(value = "#{elearningRepository}")
    private ElearningRepository elearningRepository;

    private Course course;
    private String coursename;
    private String lessonName;
    private String lessonBody;

    // create "new course"
    public String createCourse() {
        return elearningRepository.executeTransactional(new Callable<String>() {
            public String call() {
                course = new CourseAggregate(new Title(coursename), getCurrentTeacher());
                coursename = "";
                return "dashboard.xhtml";
            }
        });
    }

    // add or remove student to a course
    public String addOrRemoveStudentToCourse() {
        return elearningRepository.executeTransactional(new Callable<String>() {
            public String call() {
                Student student = getCurrentStudent();
                if (course.getParticipants().contains(student)) {
                    course.unsubscribe(student);
                } else {
                    course.subscribe(student);
                }
                return "dashboard.xhtml";
            }
        });
    }

    public Course getEntity() {
        return course;
    }

    // add lesson to a course
    public String addLessonToCourse() {
        return elearningRepository.executeTransactional(new Callable<String>() {
            public String call() {
                LessonWithoutCourse lesson = LessonFactoryBuilder.newLesson()
                                                                 .withTitle(new Title(lessonName))
                                                                 .andContent(new Content(lessonBody));
                course.addLesson(lesson);
                lessonName = "";
                lessonBody = "";
                return "course.xhtml";
            }
        });
    }

    public boolean isStudentInCourse() {
        return course == null? false: course.getParticipants().contains(getCurrentStudent());
    }

    public Title getTitle() {
        return course == null? null: course.getTitle();
    }

    public Teacher getLecturer() {
        return course == null? null: course.getLecturer();
    }

    public String getCoursename() {
        return coursename;
    }

    public void setCoursename(String name) {
        coursename = name;
    }

    public String getLessonname() {
        return lessonName;
    }

    public void setLessonname(String name) {
        lessonName = name;
    }

    public String getLessonbody() {
        return lessonBody;
    }

    public void setLessonbody(String body) {
        lessonBody = body;
    }

    public Teacher getTeacher() {
        return course == null? null: course.getLecturer();
    }

    public List<Student> getStudents() {
        return course == null? null: new ArrayList<Student>(course.getParticipants());
    }

    public List<Lesson> getLessons() {
        return course == null? null: course.getLessons();
    }

    public boolean isLessonFinished(Lesson lesson) {
        Student student = getCurrentStudent();
        if (!course.getParticipants().contains(student)) {
            return false;
        }
        int currentIndex = course.getLessons().indexOf(course.getCurrentLession(student));
        return currentIndex > course.getLessons().indexOf(lesson);
    }

    public void studentFinishesLesson() {
        Student student = getCurrentStudent();
        course.finishLesson(student, course.getCurrentLession(student));
    }

    public void setElearningRepository(ElearningRepository elearningRepository) {
        this.elearningRepository = elearningRepository;
    }

    public void setId(final Integer id) {
        if (id != null) {
            course = elearningRepository.findCourse(id);
        }
    }

    public Integer getId() {
        if (course == null) {
            return null;
        }
        return course.getId();
    }

    public Teacher getCurrentTeacher() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if (context.isUserInRole("teacher")) {
            return elearningRepository.<Teacher>findUser(new Name(context.getUserPrincipal().getName()));
        } else {
            return null;
        }
    }

    public Student getCurrentStudent() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if (context.isUserInRole("student")) {
            return elearningRepository.<Student>findUser(new Name(context.getUserPrincipal().getName()));
        } else {
            return null;
        }
    }
}
