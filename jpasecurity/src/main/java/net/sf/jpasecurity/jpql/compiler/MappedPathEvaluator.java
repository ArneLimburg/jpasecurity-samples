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
package net.sf.jpasecurity.jpql.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;

/**
 * @author Arne Limburg
 */
public class MappedPathEvaluator implements PathEvaluator {

    private MappingInformation mappingInformation;

    public MappedPathEvaluator(MappingInformation mappingInformation) {
        this.mappingInformation = mappingInformation;
    }

    public Object evaluate(Object root, String path) {
        if (root == null) {
            return null;
        }
        final Collection<?> rootCollection =
            root instanceof Collection ? (Collection<?>)root : Collections.singleton(root);
        Collection<?> result = evaluateAll(rootCollection, path);
        if (result.size() > 1) {
            throw new PersistenceException("path '" + path + "' is not single-valued");
        }
        return result.isEmpty()? null: result.iterator().next();
    }

    public List<Object> evaluateAll(final Collection<?> root, String path) {
        String[] pathElements = path.split("\\.");
        List<Object> rootCollection = new ArrayList<Object>(root);
        Set<Object> resultCollection = new HashSet<Object>();
        for (String property: pathElements) {
            resultCollection.clear();
            for (Object rootObject: rootCollection) {
                ClassMappingInformation classMapping = mappingInformation.getClassMapping(rootObject.getClass());
                PropertyMappingInformation propertyMapping = classMapping.getPropertyMapping(property);
                Object result = propertyMapping.getPropertyValue(rootObject);
                if (result instanceof Collection) {
                    resultCollection.addAll((Collection<?>)result);
                } else if (result != null) {
                    resultCollection.add(result);
                }
            }
            rootCollection.clear();
            for (Object resultObject: resultCollection) {
                if (resultObject instanceof Collection) {
                    rootCollection.addAll((Collection<Object>)resultObject);
                } else {
                    rootCollection.add(resultObject);
                }
            }
        }
        return new ArrayList<Object>(resultCollection);
    }
}
