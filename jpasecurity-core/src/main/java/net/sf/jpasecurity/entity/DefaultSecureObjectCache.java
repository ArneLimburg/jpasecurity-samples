/*
 * Copyright 2010 Arne Limburg
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
package net.sf.jpasecurity.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.BeanStore;
import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.SecureMap;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.util.SystemIdentity;

/**
 * @author Arne Limburg
 */
public class DefaultSecureObjectCache extends DefaultSecureObjectManager {

    private Map<ClassMappingInformation, Map<Object, SecureEntity>> secureEntities
        = new HashMap<ClassMappingInformation, Map<Object, SecureEntity>>();
    private Map<SystemIdentity, SecureCollection<?>> secureCollections
        = new HashMap<SystemIdentity, SecureCollection<?>>();
    private Map<SystemIdentity, SecureMap<?, ?>> secureMaps
        = new HashMap<SystemIdentity, SecureMap<?, ?>>();

    public DefaultSecureObjectCache(MappingInformation mappingInformation,
                                    BeanStore beanStore,
                                    AccessManager accessManager,
                                    Configuration configuration) {
        super(mappingInformation, beanStore, accessManager, configuration);
    }

    public SecureCollection<?> getSecureCollection(Collection<?> unsecureCollection) {
        SecureCollection<?> secureCollection = secureCollections.get(new SystemIdentity(unsecureCollection));
        if (secureCollection != null) {
            return secureCollection;
        }
        secureCollection = (SecureCollection<?>)super.getSecureObject(unsecureCollection);
        secureCollections.put(new SystemIdentity(unsecureCollection), secureCollection);
        return secureCollection;
    }

    public SecureMap<?, ?> getSecureMap(Map<?, ?> unsecureMap) {
        SecureMap<?, ?> secureMap = secureMaps.get(new SystemIdentity(unsecureMap));
        if (secureMap != null) {
            return secureMap;
        }
        secureMap = (SecureMap<?, ?>)super.getSecureObject(unsecureMap);
        secureMaps.put(new SystemIdentity(unsecureMap), secureMap);
        return secureMap;
    }

    public <E> E getReference(Class<E> type, Object id) {
        return getSecureEntity(beanStore.getReference(type, id), id);
    }

    public void detach(Object secureBean) {
        if (secureBean instanceof SecureEntity) {
            ClassMappingInformation classMapping = getClassMapping(secureBean.getClass());
            Map<Object, SecureEntity> entities = secureEntities.get(classMapping);
            if (entities != null) {
                Object id = getIdentifier(secureBean);
                entities.remove(id);
            }
        }
        super.detach(secureBean);
    }

    public <E> E getSecureObject(E unsecureObject) {
        if (unsecureObject == null) {
            return null;
        }
        if (unsecureObject instanceof Collection) {
            return (E)getSecureCollection((Collection<?>)unsecureObject);
        }
        if (unsecureObject instanceof Map) {
            return (E)getSecureMap((Map<?, ?>)unsecureObject);
        }
        Object id = null;
        if (beanStore.isLoaded(unsecureObject)) {
            id = getClassMapping(unsecureObject.getClass()).getId(unsecureObject);
        }
        return getSecureEntity(unsecureObject, id);
    }

    public <E> Collection<E> getSecureObjects(Class<E> type) {
        List<E> secureObjects = new ArrayList<E>();
        for (Map.Entry<ClassMappingInformation, Map<Object, SecureEntity>> entities: secureEntities.entrySet()) {
            if (entities.getKey().getEntityType().isAssignableFrom(type)) {
                secureObjects.addAll(((Collection<E>)entities.getValue().values()));
            }
        }
        secureObjects.addAll(super.getSecureObjects(type));
        return secureObjects;
    }

    public void preFlush() {
        super.preFlush();
        for (Map<Object, SecureEntity> entities: secureEntities.values().toArray(new Map[secureEntities.size()])) {
            for (SecureEntity entity: entities.values().toArray(new SecureEntity[entities.size()])) {
                entity.flush();
            }
        }
        executePreFlushOperations();
    }

    public void postFlush() {
        //copy over ids and version ids
        for (Map<Object, SecureEntity> entities: secureEntities.values().toArray(new Map[secureEntities.size()])) {
            Set<Object> removed = new HashSet<Object>();
            for (Map.Entry<Object, SecureEntity> entry : entities.entrySet()) {
                final SecureEntity entity = entry.getValue();
                Object unsecureObject = getUnsecureObject(entity);
                copyIdAndVersion(unsecureObject, entity);
                if (entity.isRemoved()) {
                    removed.add(entry.getKey());
                }
            }
            for (Object removedObjectKey : removed) {
                entities.remove(removedObjectKey);
            }
        }
        super.postFlush();
    }

    public void clear() {
        super.clear();
        secureEntities.clear();
        secureCollections.clear();
        secureMaps.clear();
    }

    private <E> E getSecureEntity(E unsecureObject, Object id) {
        ClassMappingInformation classMapping = getClassMapping(unsecureObject.getClass());
        Map<Object, SecureEntity> entities = secureEntities.get(classMapping);
        if (entities == null) {
            entities = new HashMap<Object, SecureEntity>();
            secureEntities.put(classMapping, entities);
        }
        SecureEntity entity = entities.get(id);
        if (entity != null) {
            return (E)entity;
        }
        Object secureObject = super.getSecureObject(unsecureObject);
        if ((secureObject instanceof SecureEntity) && id != null) {
            entities.put(id, (SecureEntity)secureObject);
            final ClassMappingInformation initializedClassMapping = getClassMapping(secureObject.getClass());
            if (initializedClassMapping != classMapping) {
                Map<Object, SecureEntity> initializedEntities = secureEntities.get(initializedClassMapping);
                if (initializedEntities == null) {
                    initializedEntities = new HashMap<Object, SecureEntity>();
                    secureEntities.put(initializedClassMapping, initializedEntities);
                }
                initializedEntities.put(id, (SecureEntity)secureObject);
            }
        }
        return (E)secureObject;
    }

    public boolean containsSecureEntity(ClassMappingInformation mapping, Object id) {
        final Map<Object, SecureEntity> objectSecureEntityMap = secureEntities.get(mapping);
        return objectSecureEntityMap != null && objectSecureEntityMap.containsKey(id);
    }

    public SecureEntity getSecureEntity(ClassMappingInformation mapping, Object id) {
        final Map<Object, SecureEntity> objectSecureEntityMap = secureEntities.get(mapping);
        return objectSecureEntityMap != null ? objectSecureEntityMap.get(id) : null;
    }
}
