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
package net.sf.jpasecurity.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Arne Limburg
 */
@Entity
public class FieldAccessMapValue {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private FieldAccessMapKey key;
    @ManyToOne
    private FieldAccessAnnotationTestBean parent;

    protected FieldAccessMapValue() {
    }

    public FieldAccessMapValue(FieldAccessMapKey key, FieldAccessAnnotationTestBean parent) {
        this.key = key;
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    public FieldAccessMapKey getKey() {
        return key;
    }

    public FieldAccessAnnotationTestBean getParent() {
        return parent;
    }

    public int hashCode() {
        if (id == 0) {
            return super.hashCode();
        }
        return id;
    }

    public boolean equals(Object object) {
        if (!(object instanceof FieldAccessMapValue)) {
            return false;
        }
        FieldAccessMapValue value = (FieldAccessMapValue)object;
        return getKey().equals(value.getKey()) && getParent().equals(value.getParent());
    }
}
