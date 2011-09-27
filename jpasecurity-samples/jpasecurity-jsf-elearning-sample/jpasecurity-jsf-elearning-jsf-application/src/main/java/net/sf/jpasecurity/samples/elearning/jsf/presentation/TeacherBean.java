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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.samples.elearning.jsf.service.ElearningRepository;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "teacher")
public class TeacherBean {

    @ManagedProperty(value = "#{elearningRepository}")
    private ElearningRepository elearningRepository;

    private Teacher teacher;

    public void setId(int id) {
        this.teacher = elearningRepository.findTeacherById(id);
    }

    public int getId() {
        return teacher.getId();
    }

    public void setName(String name) {
        this.teacher.setName(name);
    }

    public String getName() {
        return this.teacher.getName();
    }

    public List<Course> getCourses() {
        return teacher.getCourses();
    }

    public void setElearningRepository(ElearningRepository elearningRepository) {
        this.elearningRepository = elearningRepository;
    }

    @PostConstruct
    public void init() {
        teacher = new Teacher();
    }
}
