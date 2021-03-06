/*
 * Copyright 2011 Raffaela Ferrari open knowledge GmbH
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
package net.sf.jpasecurity.samples.elearning.view;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * @author Raffaela Ferrari
 */
@FacesConverter("bodyConverter")
public class BodyConverter implements Converter {

    public Object getAsObject(FacesContext aFacesContext, UIComponent aUIComponent, String s) {
        throw new UnsupportedOperationException();
    }

    public String getAsString(FacesContext aFacesContext, UIComponent aUIComponent, Object o) {
        if (o instanceof String) {
            String value = (String)o;
            value = value.replace("<", "&lt;");
            value = value.replace(">", "&gt;");
            return value.replace("\n", "<br />");
        }
        return o.toString();
    }
}
