/*
 * Copyright 2008 Mark Fisher, Juergen Hoeller
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
package org.springframework.samples.petclinic.web;

import java.beans.PropertyEditorSupport;

import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.PetType;

/**
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
public class PetTypeEditor extends PropertyEditorSupport {

    private final Clinic clinic;

    public PetTypeEditor(Clinic clinic) {
        this.clinic = clinic;
    }

    @Override
    public void setAsText(String text) {
        for (PetType type: this.clinic.getPetTypes()) {
            if (type.getName().equals(text)) {
                setValue(type);
            }
        }
    }

}
