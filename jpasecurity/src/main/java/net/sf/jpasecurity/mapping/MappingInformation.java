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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.jpasecurity.mapping.parser.JpaAnnotationParser;
import net.sf.jpasecurity.mapping.parser.OrmXmlParser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class provides mapping information for a specific persistence unit.
 * Initialized with a {@link javax.persistence.spi.PersistenceUnitInfo} it parses
 * the persistence unit and builds the mapping information.
 * @author Arne Limburg
 */
public class MappingInformation {

    private PersistenceUnitInfo persistenceUnit;
    private Map<String, String> namedQueries = new HashMap<String, String>();
    private Map<Class<?>, ClassMappingInformation> entityTypeMappings
        = new HashMap<Class<?>, ClassMappingInformation>();
    private Map<String, ClassMappingInformation> entityNameMappings;
    private ClassLoader classLoader;

    /**
     * Creates mapping information from the specified persistence unit information.
     * @param persistenceUnitInfo the persistence unit information to create the mapping information for
     */
    public MappingInformation(PersistenceUnitInfo persistenceUnitInfo) {
        persistenceUnit = persistenceUnitInfo;
        parse();
    }

    public String getPersistenceUnitName() {
        return persistenceUnit.getPersistenceUnitName();
    }

    public String getNamedQuery(String name) {
        return namedQueries.get(name);
    }

    public Collection<Class<?>> getPersistentClasses() {
        return Collections.unmodifiableSet(entityTypeMappings.keySet());
    }

    public ClassMappingInformation getClassMapping(Class<?> entityType) {
        ClassMappingInformation classMapping = entityTypeMappings.get(entityType);
        while (classMapping == null && entityType != null) {
            entityType = entityType.getSuperclass();
            classMapping = entityTypeMappings.get(entityType);
        }
        return classMapping;
    }

    public ClassMappingInformation getClassMapping(String entityName) {
        if (entityNameMappings == null) {
            initializeEntityNameMappings();
        }
        ClassMappingInformation classMapping = entityNameMappings.get(entityName);
        if (classMapping == null) {
            throw new PersistenceException("Could not find mapping for entity with name \"" + entityName + '"');
        }
        return classMapping;
    }

    public Class<?> getType(String path, Map<String, Class<?>> aliasTypes) {
        try {
            String[] entries = path.split("\\.");
            Class<?> type = aliasTypes.get(entries[0]);
            for (int i = 1; i < entries.length; i++) {
                type = getClassMapping(type).getPropertyMapping(entries[i]).getProperyType();
            }
            return type;
        } catch (NullPointerException e) {
            throw new PersistenceException("Could not determine type of alias \"" + path + "\"", e);
        }
    }

    private void initializeEntityNameMappings() {
        entityNameMappings = new HashMap<String, ClassMappingInformation>();
        for (ClassMappingInformation classMapping: entityTypeMappings.values()) {
            entityNameMappings.put(classMapping.getEntityName(), classMapping);
            entityNameMappings.put(classMapping.getEntityType().getName(), classMapping);
        }
    }

    private void parse() {
        classLoader = findClassLoader();
        JpaAnnotationParser parser = new JpaAnnotationParser(entityTypeMappings, namedQueries, classLoader);
        if (!persistenceUnit.excludeUnlistedClasses()) {
            if (persistenceUnit.getPersistenceUnitRootUrl() != null) {
                parser.parse(persistenceUnit.getPersistenceUnitRootUrl());
            }
        }
        for (URL url: persistenceUnit.getJarFileUrls()) {
            parser.parse(url);
        }
        for (String className: persistenceUnit.getManagedClassNames()) {
            try {
                Class<?> entityClass = classLoader.loadClass(className);
                parser.parse(entityClass);
            } catch (ClassNotFoundException e) {
                throw new PersistenceException(e);
            }
        }
        parse("META-INF/orm.xml");
        for (String mappingFilename: persistenceUnit.getMappingFileNames()) {
            parse(mappingFilename);
        }
        classLoader = null;
    }

    private void parse(String mappingFilename) {
        try {
            for (Enumeration<URL> mappings = classLoader.getResources(mappingFilename); mappings.hasMoreElements();) {
                parse(mappings.nextElement().openStream());
            }
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    private void parse(InputStream stream) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);
            OrmXmlParser parser = new OrmXmlParser(entityTypeMappings, namedQueries, document, classLoader);
            parser.parseNamedQueries();
            if (persistenceUnit.excludeUnlistedClasses()) {
                for (String className: persistenceUnit.getManagedClassNames()) {
                    parser.parse(classLoader.loadClass(className));
                }
                for (URL url: persistenceUnit.getJarFileUrls()) {
                    parser.parse(url);
                }
            } else {
                for (Node node: parser.getEntityNodes()) {
                    parser.parse(classLoader.loadClass(getClassName(node)));
                }
                for (Node node: parser.getSuperclassNodes()) {
                    parser.parse(classLoader.loadClass(getClassName(node)));
                }
                for (Node node: parser.getEmbeddableNodes()) {
                    parser.parse(classLoader.loadClass(getClassName(node)));
                }
            }
        } catch (ParserConfigurationException e) {
            throw new PersistenceException(e);
        } catch (SAXException e) {
            throw new PersistenceException(e);
        } catch (IOException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        } finally {
            stream.close();
        }
    }

    private String getClassName(Node classNode) {
        Node classAttribute = classNode.getAttributes().getNamedItem(OrmXmlParser.CLASS_ATTRIBUTE_NAME);
        return classAttribute.getNodeValue();
    }

    private ClassLoader findClassLoader() {
        ClassLoader classLoader = persistenceUnit.getNewTempClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        classLoader = persistenceUnit.getClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }
}