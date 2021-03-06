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
package net.sf.jpasecurity.security.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.jpasecurity.configuration.AccessRule;
import net.sf.jpasecurity.configuration.AccessRulesProvider;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.configuration.ConfigurationReceiver;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.configuration.SecurityContextReceiver;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.MappingInformationReceiver;

/**
 * This implementation of the {@link AccessRulesProvider} interface.
 * @author Arne Limburg
 */
public class DefaultAccessRulesProvider implements AccessRulesProvider,
                                                   MappingInformationReceiver,
                                                   SecurityContextReceiver,
                                                   ConfigurationReceiver {

    private final AnnotationAccessRulesProvider annotationRulesProvider = new AnnotationAccessRulesProvider();
    private final XmlAccessRulesProvider xmlRulesProvider = new XmlAccessRulesProvider();
    private List<AccessRule> accessRules;

    public List<AccessRule> getAccessRules() {
        if (accessRules == null) {
            List<AccessRule> accessRules = new ArrayList<AccessRule>();
            accessRules.addAll(annotationRulesProvider.getAccessRules());
            accessRules.addAll(xmlRulesProvider.getAccessRules());
            this.accessRules = Collections.unmodifiableList(accessRules);
        }
        return accessRules;
    }

    public void setMappingInformation(MappingInformation persistenceMapping) {
        annotationRulesProvider.setMappingInformation(persistenceMapping);
        xmlRulesProvider.setMappingInformation(persistenceMapping);
    }

    public void setMappingProperties(Map<String, Object> properties) {
        annotationRulesProvider.setMappingProperties(properties);
        xmlRulesProvider.setMappingProperties(properties);
    }

    public void setSecurityContext(SecurityContext securityContext) {
        annotationRulesProvider.setSecurityContext(securityContext);
        xmlRulesProvider.setSecurityContext(securityContext);
    }

    public void setConfiguration(Configuration configuration) {
        annotationRulesProvider.setConfiguration(configuration);
        xmlRulesProvider.setConfiguration(configuration);
    }
}
