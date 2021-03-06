/*
 * Copyright 2010 - 2011 Arne Limburg
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.SecureMap;
import net.sf.jpasecurity.SecureObject;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.mapping.BeanInitializer;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.CollectionValuedRelationshipMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.RelationshipMappingInformation;
import net.sf.jpasecurity.mapping.SingleValuedRelationshipMappingInformation;
import net.sf.jpasecurity.proxy.Decorator;
import net.sf.jpasecurity.proxy.EntityProxy;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;

import static net.sf.jpasecurity.util.Types.isSimplePropertyType;
import static net.sf.jpasecurity.util.Validate.notNull;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecureObjectManager implements SecureObjectManager {

    protected final Configuration configuration;
    private final MappingInformation mappingInformation;
    private final AccessManager accessManager;
    private final List<Runnable> preFlushOperations = new ArrayList<Runnable>();
    private final List<Runnable> postFlushOperations = new ArrayList<Runnable>();

    public AbstractSecureObjectManager(MappingInformation mappingInformation,
                                       AccessManager accessManager,
                                       Configuration configuration) {
        notNull(MappingInformation.class, mappingInformation);
        notNull(Configuration.class, configuration);
        notNull(AccessManager.class, accessManager);
        this.mappingInformation = mappingInformation;
        this.configuration = configuration;
        this.accessManager = accessManager;
    }

    protected void addPreFlushOperation(Runnable operation) {
        preFlushOperations.add(operation);
    }

    protected void addPostFlushOperation(Runnable operation) {
        postFlushOperations.add(operation);
    }

    public void postFlush() {
        try {
            for (Runnable operation: postFlushOperations) {
                operation.run();
            }
        } finally {
            postFlushOperations.clear();
        }
    }

    public void executePreFlushOperations() {
        try {
            for (Runnable operation: preFlushOperations) {
                operation.run();
            }
        } finally {
            preFlushOperations.clear();
        }
    }

    public boolean isSecureObject(Object object) {
        return object instanceof SecureObject;
    }

    public <T> T getSecureObject(T object) {
        if (object == null) {
            return null;
        }
        if (object instanceof List) {
            return (T)createSecureList((List<?>)object, this, accessManager);
        } else if (object instanceof SortedSet) {
            return (T)createSecureSortedSet((SortedSet<?>)object, this, accessManager);
        } else if (object instanceof Set) {
            return (T)createSecureSet((Set<?>)object, this, accessManager);
        } else if (object instanceof Collection) {
            return (T)createSecureCollection((Collection<?>)object, this, accessManager);
        } else if (object instanceof Map) {
            return (T)createSecureMap((Map<?, ?>)object, this, accessManager);
        } else {
            return createSecureEntity(object, false);
        }
    }

    public <T> T createSecureEntity(T object, boolean isTransient) {
        ClassMappingInformation mapping = getClassMapping(object.getClass());
        BeanInitializer beanInitializer = configuration.getBeanInitializer();
        if (!mapping.getSubclassMappings().isEmpty()) {
            // object may be superclass proxy of wrong type, so we have to initialize it
            object = beanInitializer.initialize(object);
            mapping = getClassMapping(object.getClass());
        }
        MethodInterceptor interceptor = createInterceptor(beanInitializer, object);
        Decorator<SecureEntity> decorator
            = createDecorator(mapping, beanInitializer, accessManager, object, isTransient);
        return createSecureEntity(mapping.<T>getEntityType(), interceptor, decorator);
    }

    <E> E createSecureEntity(Class<E> type, MethodInterceptor interceptor, Decorator<SecureEntity> decorator) {
        return (E)configuration.getSecureEntityProxyFactory().createSecureEntityProxy(type, interceptor, decorator);
    }

    private MethodInterceptor createInterceptor(BeanInitializer beanInitializer, Object object) {
        return configuration.createMethodInterceptor(beanInitializer, this, object);
    }

    private Decorator<SecureEntity> createDecorator(ClassMappingInformation mapping, BeanInitializer beanInitializer,
                                              AccessManager accessManager, Object object, boolean isTransient) {
        return configuration.createDecorator(mapping, beanInitializer, accessManager, this, object, isTransient);
    }

    boolean containsUnsecureObject(Object secureObject) {
        if (secureObject == null) {
            return true;
        }
        secureObject = unwrap(secureObject);
        return secureObject instanceof SecureObject;
    }

    <T> T getUnsecureObject(T secureObject) {
        return getUnsecureObject(secureObject, true);
    }

    <T> T getUnsecureObject(T secureObject, boolean create) {
        if (secureObject == null) {
            return null;
        }
        secureObject = unwrap(secureObject);
        if (secureObject instanceof SecureEntity) {
            final SecureEntityProxyFactory proxyFactory = configuration.getSecureEntityProxyFactory();
            final MethodInterceptor interceptor = proxyFactory.getInterceptor((SecureEntity)secureObject);
            if (interceptor instanceof SecureEntityInterceptor) {
                final SecureEntityInterceptor secureEntityInterceptor
                    = (SecureEntityInterceptor)interceptor;
                return (T)secureEntityInterceptor.entity;
            }
        } else if (secureObject instanceof AbstractSecureCollection) {
            return (T)((AbstractSecureCollection<?, Collection<?>>)secureObject).getOriginal();
        } else if (secureObject instanceof SecureList) {
            return (T)((SecureList<?>)secureObject).getOriginal();
        } else if (secureObject instanceof DefaultSecureMap) {
            return (T)((DefaultSecureMap<?, ?>)secureObject).getOriginal();
        }
        if (create) {
            return createUnsecureObject(secureObject);
        }
        throw new IllegalArgumentException("Cannot retrieve unsecure version from given secure object");
    }

    abstract <T> T createUnsecureObject(T secureObject);

    void secureCopy(Object unsecureObject, Object secureObject) {
        ClassMappingInformation classMapping = getClassMapping(secureObject.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            Object value = propertyMapping.getPropertyValue(unsecureObject);
            if (propertyMapping.isRelationshipMapping()) {
                RelationshipMappingInformation relationshipMapping = (RelationshipMappingInformation)propertyMapping;
                if (propertyMapping.isManyValued() || !relationshipMapping.getRelatedClassMapping().isEmbeddable()
                    || !configuration.treatEmbeddablesAsSimpleValues()) {
                    value = getSecureObject(value);
                }
            }
            propertyMapping.setPropertyValue(secureObject, value);
        }
    }

    void unsecureCopy(final AccessType accessType, final Object secureObject, final Object unsecureObject) {
        if (secureObject instanceof SecureObject && !((SecureObject)secureObject).isInitialized()) {
            return;
        }
        boolean modified = false;
        final ClassMappingInformation classMapping = getClassMapping(secureObject.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isVersionProperty()
                            || (propertyMapping.isIdProperty() && propertyMapping.isGeneratedValue())) {
                continue; //don't change id or version property
            }
            if (propertyMapping.isManyValued()) {
                modified = unsecureCopyCollectionProperty(classMapping,
                                                          propertyMapping,
                                                          accessType,
                                                          secureObject,
                                                          unsecureObject,
                                                          modified);
            } else {
                Object secureValue = propertyMapping.getPropertyValue(secureObject);
                Object newValue;
                if (propertyMapping.isRelationshipMapping()) {
                    RelationshipMappingInformation relationshipMapping
                        = (RelationshipMappingInformation)propertyMapping;
                    if (relationshipMapping.getRelatedClassMapping().isEmbeddable()
                        && configuration.treatEmbeddablesAsSimpleValues()) {
                        newValue = secureValue;
                    } else {
                        newValue = getUnsecureObject(secureValue);
                    }
                } else {
                    newValue = secureValue;
                }
                modified = setPropertyValue(classMapping,
                                            propertyMapping,
                                            accessType,
                                            secureObject,
                                            unsecureObject,
                                            modified,
                                            newValue);
            }
        }
    }

    /**
     * @return <tt>true</tt>, if the owner is modified, <tt>false</tt> otherwise.
     */
    private boolean setPropertyValue(final ClassMappingInformation classMapping,
                                     PropertyMappingInformation propertyMapping, final AccessType accessType,
                                     final Object secureObject, final Object unsecureObject, boolean modified,
                                     Object newValue) {
        Object oldValue = propertyMapping.getPropertyValue(unsecureObject);
        if (isDirty(propertyMapping, newValue, oldValue)) {
            if (!modified) {
                checkAccess(accessType, secureObject);
                if (accessType == AccessType.UPDATE) {
                    fireLifecycleEvent(accessType, classMapping, secureObject);
                }
            }
            propertyMapping.setPropertyValue(unsecureObject, newValue);
            modified = true;
        }
        return modified;
    }

    /**
     * @return <tt>true</tt>, if the owner is modified, <tt>false</tt> otherwise.
     */
    void unsecureCopyCollections(final AccessType accessType, final Object secureObject, final Object unsecureObject) {
        if (secureObject instanceof SecureObject && !((SecureObject)secureObject).isInitialized()) {
            return;
        }
        boolean modified = false;
        final ClassMappingInformation classMapping = getClassMapping(secureObject.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isManyValued()) {
                modified = unsecureCopyCollectionProperty(classMapping,
                                                          propertyMapping,
                                                          accessType,
                                                          secureObject,
                                                          unsecureObject,
                                                          modified);
            }
        }
    }

    /**
     * @return <tt>true</tt>, if the owner is modified, <tt>false</tt> otherwise.
     */
    private boolean unsecureCopyCollectionProperty(final ClassMappingInformation classMapping,
                                                   final PropertyMappingInformation propertyMapping,
                                                   final AccessType accessType,
                                                   final Object secureObject,
                                                   final Object unsecureObject,
                                                   boolean modified) {
        Object secureValue = propertyMapping.getPropertyValue(secureObject);
        if (secureValue instanceof SecureCollection) {
            modified = secureCopy(classMapping,
                                  propertyMapping,
                                  accessType,
                                  secureObject,
                                  unsecureObject,
                                  (SecureCollection<?>)secureValue,
                                  modified);
        } else if (secureValue instanceof SecureMap) {
            modified = secureCopy(classMapping,
                                  propertyMapping,
                                  accessType,
                                  secureObject,
                                  unsecureObject,
                                  (SecureMap<?, ?>)secureValue,
                                  modified);
        } else {
            Object newValue;
            if (secureValue instanceof Collection) {
                newValue = createUnsecureCollection((Collection<?>)secureValue);
            } else if (secureValue instanceof Map) {
                newValue = createUnsecureMap((Map<?, ?>)secureValue);
            } else if (secureValue == null) {
                newValue = null;
            } else {
                throw new IllegalStateException("Unsupported collection type: " + secureValue.getClass().getName());
            }
            modified = setPropertyValue(classMapping, propertyMapping, accessType, secureObject, unsecureObject,
                                        modified, newValue);
        }
        return modified;
    }

    private <K, V> Map<K, V> createUnsecureMap(Map<K, V> secureValue) {
        Map<K, V> unsecureMap = new HashMap<K, V>();
        for (Map.Entry<K, V> entry: secureValue.entrySet()) {
            K key = entry.getKey();
            if (!isSimplePropertyType(key.getClass())) {
                key = getUnsecureObject(key);
            }
            unsecureMap.put(key, getUnsecureObject(entry.getValue()));
        }
        return unsecureMap;
    }

    private <E, T extends List<E>> SecureList<E> createSecureList(T list,
                                                                  AbstractSecureObjectManager objectManager,
                                                                  AccessManager accessManager) {
        return new SecureList<E>(list, objectManager, accessManager);
    }

    private <E, T extends SortedSet<E>> SecureSortedSet<E> createSecureSortedSet(T sortedSet,
                                                                                 AbstractSecureObjectManager manager,
                                                                                 AccessManager accessManager) {
        return new SecureSortedSet<E>(sortedSet, manager, accessManager);
    }

    private <E, T extends Set<E>> SecureSet<E> createSecureSet(T set,
                                                               AbstractSecureObjectManager objectManager,
                                                               AccessManager accessManager) {
        return new SecureSet<E>(set, objectManager, accessManager);
    }

    private <E, T extends Collection<E>> SecureCollection<E> createSecureCollection(T collection,
                                                                                    AbstractSecureObjectManager mgr,
                                                                                    AccessManager accessManager) {
        return new DefaultSecureCollection<E, T>(collection, mgr, accessManager);
    }

    private <K, V> SecureMap<K, V> createSecureMap(Map<K, V> original,
                                                   AbstractSecureObjectManager objectManager,
                                                   AccessManager accessManager) {
        return new DefaultSecureMap<K, V>(original, objectManager, accessManager);
    }

    private <T> Collection<T> createUnsecureCollection(Collection<T> secureValue) {
        Collection<T> unsecureCollection = createCollection(secureValue);
        for (T entry: secureValue) {
            unsecureCollection.add(getUnsecureObject(entry));
        }
        return unsecureCollection;
    }

    /**
     * @return <tt>true</tt>, if the owner is modified, <tt>false</tt> otherwise.
     */
    <V> boolean secureCopy(ClassMappingInformation classMapping,
                           PropertyMappingInformation propertyMapping,
                           AccessType accessType,
                           Object secureOwner,
                           Object unsecureOwner,
                           SecureCollection<V> secureValue,
                           boolean modified) {
        Collection<V> unsecureCollection = getUnsecureObject(secureValue);
        SecureCollection<V> secureCollection = (SecureCollection<V>)getSecureObject(unsecureCollection);
        if (secureValue != secureCollection && secureValue.isDirty()) {
            secureCollection = secureValue.merge(secureCollection);
        }
        if (secureCollection.isDirty()) {
            if (!modified) {
                checkAccess(accessType, secureOwner);
                fireLifecycleEvent(accessType, classMapping, secureOwner);
            }
            if (accessType == AccessType.UPDATE) {
                if (secureCollection instanceof AbstractSecureCollection) {
                    ((AbstractSecureCollection<?, ?>)secureCollection).flush();
                } else if (secureCollection instanceof SecureList) {
                    ((SecureList<V>)secureCollection).flush();
                } else {
                    throw new IllegalStateException("unsupported secure collection type: " + secureCollection.getClass());
                }
            }
            modified = true;
        }
        if (secureCollection != secureValue) {
            propertyMapping.setPropertyValue(unsecureOwner, getUnsecureObject(secureCollection));
        }
        return modified;
    }

    /**
     * @return <tt>true</tt>, if the owner is modified, <tt>false</tt> otherwise.
     */
    <K, V> boolean secureCopy(ClassMappingInformation classMapping,
                              PropertyMappingInformation propertyMapping,
                              AccessType accessType,
                              Object secureOwner,
                              Object unsecureOwner,
                              SecureMap<K, V> secureValue,
                              boolean modified) {
        Map<K, V> unsecureMap = getUnsecureObject(secureValue);
        SecureMap<K, V> secureMap = (SecureMap<K, V>)getSecureObject(unsecureMap);
        if (secureValue != secureMap && secureValue.isDirty()) {
            secureMap = secureValue.merge(secureMap);
        }
        if (secureMap.isDirty()) {
            if (!modified) {
                checkAccess(accessType, secureOwner);
                fireLifecycleEvent(accessType, classMapping, secureOwner);
            }
            if (accessType == AccessType.UPDATE) {
                if (secureMap instanceof DefaultSecureMap) {
                    ((DefaultSecureMap<K, V>)secureMap).flush();
                } else {
                    throw new IllegalStateException("unsupported secure map type: " + secureMap.getClass());
                }
            }
            modified = true;
        }
        if (secureMap != secureValue) {
            propertyMapping.setPropertyValue(unsecureOwner, getUnsecureObject(secureMap));
        }
        return modified;
    }

    void copyIdAndVersion(Object source, Object target) {
        source = unwrap(source);
        target = unwrap(target);
        if (source instanceof SecureObject && !((SecureObject)source).isInitialized()) {
            return;
        }
        if (target instanceof SecureObject && !((SecureObject)target).isInitialized()) {
            return;
        }
        ClassMappingInformation classMapping = getClassMapping(target.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getIdPropertyMappings()) {
            Object newId = propertyMapping.getPropertyValue(source);
            if (propertyMapping.isRelationshipMapping()) {
                newId = getSecureObject(newId);
            }
            propertyMapping.setPropertyValue(target, newId);
        }
        for (PropertyMappingInformation propertyMapping: classMapping.getVersionPropertyMappings()) {
            Object newVersion = propertyMapping.getPropertyValue(source);
            propertyMapping.setPropertyValue(target, newVersion);
        }
    }

    boolean isDirty(Object newEntity, Object oldEntity) {
        final ClassMappingInformation classMapping = getClassMapping(newEntity.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isIdProperty() || propertyMapping.isVersionProperty()) {
                continue; //don't change id or version property
            }
            Object newValue = propertyMapping.getPropertyValue(newEntity);
            Object oldValue = propertyMapping.getPropertyValue(oldEntity);
            if (isDirty(propertyMapping, newValue, oldValue)) {
                return true;
            }
        }
        return false;
    }

    boolean isDirty(PropertyMappingInformation propertyMapping, Object newValue, Object oldValue) {
        if (newValue == oldValue) {
            return false;
        }
        if (!propertyMapping.isRelationshipMapping()) {
            return !nullSaveEquals(newValue, oldValue);
        } else if (propertyMapping.isSingleValued()) {
            if (null == newValue || null == oldValue) {
                // if one is null the other one can not be null (see first check)
                return true;
            }
            // both values are not null, so we can compare ids
            SingleValuedRelationshipMappingInformation relationshipMapping =
                (SingleValuedRelationshipMappingInformation)propertyMapping;
            Object newId = relationshipMapping.getRelatedClassMapping().getId(newValue);
            Object oldId = relationshipMapping.getRelatedClassMapping().getId(oldValue);
            return !nullSaveEquals(newId, oldId);
        } else {
            CollectionValuedRelationshipMappingInformation relationshipMapping
                = (CollectionValuedRelationshipMappingInformation)propertyMapping;
            if (Collection.class.isAssignableFrom(relationshipMapping.getCollectionType())) {
                Collection<?> oldCollection = (Collection<?>)oldValue;
                Collection<?> newCollection = (Collection<?>)newValue;
                return oldCollection == null
                    || newCollection == null
                    || oldCollection.size() != newCollection.size()
                    || !oldCollection.containsAll(newCollection)
                    || !newCollection.containsAll(oldCollection);
            } else if (Map.class.isAssignableFrom(relationshipMapping.getCollectionType())) {
                Map<?, ?> oldMap = (Map<?, ?>)oldValue;
                Map<?, ?> newMap = (Map<?, ?>)newValue;
                return oldMap == null
                    || newMap == null
                    || oldMap.size() != newMap.size()
                    || !oldMap.entrySet().containsAll(newMap.entrySet())
                    || !newMap.entrySet().containsAll(oldMap.entrySet());
            } else {
                ExceptionFactory exceptionFactory = configuration.getExceptionFactory();
                throw exceptionFactory.createTypeNotFoundException(relationshipMapping.getCollectionType());
            }
        }
    }

    ClassMappingInformation getClassMapping(Class<?> type) {
        return mappingInformation.getClassMapping(type);
    }

    boolean isAccessible(AccessType accessType, Object entity) {
        return accessManager.isAccessible(accessType, entity);
    }

    void checkAccess(AccessType accessType, Object entity) {
        if (!accessManager.isAccessible(accessType, entity)) {
            throw new SecurityException("The current user is not permitted to " + accessType.toString().toLowerCase() + " the specified object of type " + getClassMapping(entity.getClass()).getEntityType().getName());
        }
    }

    void fireLifecycleEvent(AccessType accessType, ClassMappingInformation classMapping, Object entity) {
        switch (accessType) {
            case CREATE:
                firePersist(classMapping, entity);
                break;
            case UPDATE:
                fireUpdate(classMapping, entity);
                break;
            case DELETE:
                fireRemove(classMapping, entity);
                break;
            default:
                throw new IllegalArgumentException("unsupported accessType " + accessType);
        }
    }

    void firePersist(final ClassMappingInformation classMapping, final Object entity) {
        classMapping.prePersist(entity);
        addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postPersist(entity);
            }
        });
    }

    void fireUpdate(final ClassMappingInformation classMapping, final Object entity) {
        addPreFlushOperation(new Runnable() {
            public void run() {
                classMapping.preUpdate(entity);
            }
        });
        addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postUpdate(entity);
            }
        });
    }

    void fireRemove(final ClassMappingInformation classMapping, final Object entity) {
        addPreFlushOperation(new Runnable() {
            public void run() {
                classMapping.preRemove(entity);
            }
        });
        addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postRemove(entity);
            }
        });
    }

    void setRemoved(SecureEntity secureEntity) {
        SecureEntityDecorator secureEntityDecorator
            = (SecureEntityDecorator)configuration.getSecureEntityProxyFactory().getDecorator(secureEntity);
        secureEntityDecorator.deleted = true;
    }

    void initialize(SecureEntity secureEntity, boolean checkAccess) {
        SecureEntityDecorator secureEntityDecorator
            = (SecureEntityDecorator)configuration.getSecureEntityProxyFactory().getDecorator(secureEntity);
        secureEntityDecorator.refresh(checkAccess);
    }

    <B> B unwrap(B bean) {
        if (bean instanceof EntityProxy) {
            return ((EntityProxy)bean).<B>getEntity();
        } else {
            return bean;
        }
    }

    private <T> Collection<T> createCollection(Collection<T> original) {
        if (original instanceof SortedSet) {
            return new TreeSet<T>(((SortedSet<T>)original).comparator());
        } else if (original instanceof Set) {
            return new LinkedHashSet<T>();
        } else {
            return new ArrayList<T>();
        }
    }

    private boolean nullSaveEquals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null) {
            return false;
        }
        return object1.equals(object2);
    }
}
