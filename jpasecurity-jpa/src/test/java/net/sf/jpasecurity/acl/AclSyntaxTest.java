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
package net.sf.jpasecurity.acl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import net.sf.jpasecurity.model.acl.Acl;
import net.sf.jpasecurity.model.acl.AclEntry;
import net.sf.jpasecurity.model.acl.AclProtectedEntity;
import net.sf.jpasecurity.model.acl.Group;
import net.sf.jpasecurity.model.acl.Privilege;
import net.sf.jpasecurity.model.acl.Role;
import net.sf.jpasecurity.model.acl.User;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** @author Arne Limburg */
public class AclSyntaxTest {

    private static EntityManagerFactory entityManagerFactory;
    private static Group group;
    private static Privilege privilege1;
    private static Privilege privilege2;
    private static User user;
    private static User user2;
    private static AclProtectedEntity entity;
    private static final int FULL_ACCESS_PRIVILEGE = -2;
    private static final int READ_ACCESS_PRIVILEGE = -1;

    @BeforeClass
    public static void createEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("acl-model-nocache");
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        entityManagerFactory.close();
        entityManagerFactory = null;
    }

    @Before
    public void createTestData() {
        TestAuthenticationProvider.authenticate(FULL_ACCESS_PRIVILEGE, FULL_ACCESS_PRIVILEGE);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        if (user == null) {
            entityManager.getTransaction().begin();
            privilege1 = new Privilege();
            privilege1.setName("modifySomething");
            entityManager.persist(privilege1);
            privilege2 = new Privilege();
            privilege2.setName("viewSomething");
            entityManager.persist(privilege2);
            group = new Group();
            group.setName("USERS");
            group.getFullHierarchy().add(group);
            entityManager.persist(group);
            Group group2 = new Group();
            group2.setName("ADMINS");
            group2.getFullHierarchy().add(group2);
            entityManager.persist(group2);
            Role role = new Role();
            role.setName("Test Role");
            //       role.setPrivileges(Arrays.asList(privilege1, privilege2));
            entityManager.persist(role);
            user = new User();
            user.setGroups(Arrays.asList(group));
            user.setRoles(Arrays.asList(role));
            entityManager.persist(user);
            user2 = new User();
            user2.setGroups(Arrays.asList(group2));
            user2.setRoles(Arrays.asList(role));
            entityManager.persist(user);
            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();
            TestAuthenticationProvider.authenticate(user.getId(), Arrays.asList());

            Acl acl = new Acl();
            entityManager.persist(acl);
            AclEntry entry = new AclEntry();
            entry.setAccessControlList(acl);
            acl.getEntries().add(entry);
            //       entry.setPrivilege(privilege1);
            entry.setGroup(group);
            entityManager.persist(entry);

            entity = new AclProtectedEntity();
            entity.setAccessControlList(acl);
            entityManager.persist(entity);

            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    @After
    public void logout() {
        TestAuthenticationProvider.authenticate(null);
    }

    @Test
    public void queryAclProtectedEntity() {
        TestAuthenticationProvider.authenticate(FULL_ACCESS_PRIVILEGE, FULL_ACCESS_PRIVILEGE);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            AclProtectedEntity entity =
                (AclProtectedEntity)entityManager.createQuery("select e from AclProtectedEntity e")
                    .getSingleResult();
            assertNotNull(entity);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            entityManager.close();
        }
    }

    @Test
    public void queryAclProtectedEntityWithNoPrivileges() {
        TestAuthenticationProvider.authenticate(user2.getId());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            try {
                entityManager.createQuery("select e from AclProtectedEntity e").getSingleResult();
                fail();
            } catch (NoResultException e) {
                //expected
            }
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    @Test
    public void queryAclProtectedEntityWithReadAllPrivilege() {
        TestAuthenticationProvider.authenticate(user2.getId(), READ_ACCESS_PRIVILEGE);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            final List<AclProtectedEntity> resultList =
                entityManager.createQuery("select e from AclProtectedEntity e").getResultList();
            assertNotNull(resultList);
            assertEquals(1, resultList.size());
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    @Test
    public void updateAclProtectedEntityWithReadAllPrivilege() {
        TestAuthenticationProvider.authenticate(user2.getId(), READ_ACCESS_PRIVILEGE);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            final AclProtectedEntity result =
                entityManager.find(AclProtectedEntity.class, entity.getId());
            assertNotNull(result);
            result.setSomeProperty("OtherValue");
            try {
                transaction.commit();
                fail("Expect SecurityException");
            } catch (SecurityException e) {
                //expected
            }
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    @Test
    public void queryAclProtectedEntityWithFullAccessPrivilege() {
        TestAuthenticationProvider.authenticate(user2.getId(), FULL_ACCESS_PRIVILEGE);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            final List<AclProtectedEntity> resultList =
                entityManager.createQuery("select e from AclProtectedEntity e").getResultList();
            assertNotNull(resultList);
            assertEquals(1, resultList.size());
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    @Test
    public void updateAclProtectedEntity() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            TestAuthenticationProvider.authenticate(user.getId());
            transaction.begin();
            entityManager.find(User.class, user.getId());
            AclProtectedEntity e = entityManager.find(AclProtectedEntity.class, entity.getId());
            entity.getAccessControlList().getEntries().size();
            e.setSomeProperty("test");
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    @Test
    public void updateAclProtectedEntityNoAccess() {
        TestAuthenticationProvider.authenticate(user2.getId());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.find(User.class, user.getId());
            AclProtectedEntity e = entityManager.find(AclProtectedEntity.class, entity.getId());
            entity.getAccessControlList().getEntries().size();
            e.setSomeProperty("test");
            transaction.commit();
            fail("Expect exception!");
        } catch (SecurityException e) {
            //Expected
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    @Test
    public void updateAclProtectedEntityNoAccessOnlyFullRead() {
        TestAuthenticationProvider.authenticate(user2.getId(), READ_ACCESS_PRIVILEGE);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.find(User.class, user.getId());
            AclProtectedEntity e = entityManager.find(AclProtectedEntity.class, entity.getId());
            entity.getAccessControlList().getEntries().size();
            e.setSomeProperty("test" + System.currentTimeMillis());
            transaction.commit();
            fail("Expect exception!");
        } catch (SecurityException e) {
            //Expected
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    @Test
    public void updateAclProtectedEntityFullAccessPrivilege() {
        TestAuthenticationProvider.authenticate(user2.getId(), FULL_ACCESS_PRIVILEGE);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.find(User.class, user.getId());
            AclProtectedEntity e = entityManager.find(AclProtectedEntity.class, entity.getId());
            entity.getAccessControlList().getEntries().size();
            e.setSomeProperty("test" + System.currentTimeMillis());
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }
}
