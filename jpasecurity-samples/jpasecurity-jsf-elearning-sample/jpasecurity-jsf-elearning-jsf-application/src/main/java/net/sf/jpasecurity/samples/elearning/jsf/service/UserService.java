/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import javax.el.ELContext;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;

/**
 * @author Arne Limburg
 */
@RequestScoped @ManagedBean
public class UserService implements net.sf.jpasecurity.sample.elearning.domain.UserService {

    @ManagedProperty(value = "#{userRepository}")
    private UserRepository userRepository;

    public <U extends User> U getCurrentUser() {
        return userRepository.<U>findUser(getCurrentUserName());
    }

    public Name getCurrentUserName() {
        String user = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        return user != null? new Name(user): null;
    }

    public boolean isSubscribed() {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Object courseBean = elContext.getELResolver().getValue(elContext, null, "course");
        Course currentCourse = (Course)elContext.getELResolver().getValue(elContext, courseBean, "entity");
        return currentCourse != null? currentCourse.getParticipants().contains(getCurrentUser()): false;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
