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
package net.sf.jpasecurity.security.authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.persistence.PersistenceInformationReceiver;
import net.sf.jpasecurity.security.AuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class EjbAuthenticationProvider implements AuthenticationProvider, PersistenceInformationReceiver {

    private Set<String> roles;

    public void setPersistenceMapping(MappingInformation persistenceMapping) {
        roles = new DeclareRolesParser().parseDeclaredRoles(persistenceMapping.getPersistentClasses());
    }

    public Object getPrincipal() {
        return getContext().getCallerPrincipal().getName();
    }

    public Collection<String> getRoles() {
        EJBContext context = getContext();
        List<String> filteredRoles = new ArrayList<String>();
        for (String role: roles) {
            if (context.isCallerInRole(role)) {
                filteredRoles.add(role);
            }
        }
        return filteredRoles;
    }

    protected EJBContext getContext() {
        try {
            InitialContext context = new InitialContext();
            return (EJBContext)context.lookup("java:comp/EJBContext");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public void setPersistenceProperties(Map<String, String> properties) {
        //not needed
    }
}