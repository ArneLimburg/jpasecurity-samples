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
package net.sf.jpasecurity.mapping;

import java.util.Collection;
import java.util.Set;

/**
 * This interface represents mapping information for a specific security unit.
 * @author Arne Limburg
 */
public interface MappingInformation {

    String getSecurityUnitName();
    Set<String> getNamedQueryNames();
    String getNamedQuery(String name);
    Collection<Class<?>> getSecureClasses();
    boolean containsClassMapping(Class<?> entityType);
    boolean containsClassMapping(String entityName);
    ClassMappingInformation getClassMapping(Class<?> entityType);
    ClassMappingInformation getClassMapping(String entityName);
    Collection<ClassMappingInformation> resolveClassMappings(Class<?> type);
    boolean isMapPath(Path path, Set<TypeDefinition> typeDefinitions);
    Class<?> getKeyType(Alias alias, Set<TypeDefinition> typeDefinitions);
    Class<?> getKeyType(Path path, Set<TypeDefinition> typeDefinitions);
    <T> Class<T> getType(Alias alias, Set<TypeDefinition> typeDefinitions);
    <T> Class<T> getType(Path path, Set<TypeDefinition> typeDefinitions);
    PropertyMappingInformation getPropertyMapping(Path path, Set<TypeDefinition> typeDefinitions);
    PropertyMappingInformation getPropertyMapping(Class<?> rootType, Path path);

    Set<String> getNamedNativeQueryNames();

    String getNamedNativeQuery(String name);
}
