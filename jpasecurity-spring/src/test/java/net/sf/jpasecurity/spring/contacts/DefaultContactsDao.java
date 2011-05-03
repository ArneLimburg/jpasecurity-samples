/*
 * Copyright 2008 Arne Limburg
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
package net.sf.jpasecurity.spring.contacts;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.sf.jpasecurity.contacts.model.Contact;
import net.sf.jpasecurity.contacts.model.User;

/**
 * @author Arne Limburg
 */
public class DefaultContactsDao implements ContactsDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<User> getAllUsers() {
        return entityManager.createQuery("SELECT user FROM User user").getResultList();
    }

    public User getUser(String name) {
        return (User)entityManager.createQuery("SELECT user FROM User user WHERE user.name = :name")
                                  .setParameter("name", name)
                                  .getSingleResult();
    }

    public List<Contact> getAllContacts() {
        return entityManager.createQuery("SELECT contact FROM Contact contact").getResultList();
    }
}
