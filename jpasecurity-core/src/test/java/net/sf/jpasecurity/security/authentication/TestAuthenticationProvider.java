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

import java.util.Map;

import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.MappingInformationReceiver;

/**
 * @author Arne Limburg
 */
public class TestAuthenticationProvider extends StaticAuthenticationProvider
                                        implements MappingInformationReceiver {

    private static MappingInformation persistenceMapping;
    private static Map<String, Object> persistenceProperties;

    public static MappingInformation getPersistenceMapping() {
        return persistenceMapping;
    }

    public void setMappingInformation(MappingInformation persistenceMapping) {
        TestAuthenticationProvider.persistenceMapping = persistenceMapping;
    }

    public static Map<String, Object> getPersistenceProperties() {
        return persistenceProperties;
    }

    public void setMappingProperties(Map<String, Object> properties) {
        TestAuthenticationProvider.persistenceProperties = properties;
    }
}
