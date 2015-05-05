/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.rest.documentation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.rest.documentation.javadoc.ClassDescriptor;
import org.springframework.rest.documentation.javadoc.Javadoc;
import org.springframework.rest.documentation.javadoc.MethodDescriptor;
import org.springframework.rest.documentation.model.Documentation;
import org.springframework.rest.documentation.model.Endpoint;

public class DocumentationGenerator {

	private final ApplicationContext applicationContext;

	private Javadoc javadoc;

	public DocumentationGenerator(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Documentation generate(InputStream javadocInputStream) {
		try {
			this.javadoc = new ObjectMapper().readValue(javadocInputStream, Javadoc.class);
		} catch (Exception e) {
			throw new FatalBeanException("Failed to load javadoc JSON", e);
		}
		List<Endpoint> endpoints = new EndpointDiscoverer(this.javadoc, this.applicationContext).discoverEndpoints();
		Map<String, ClassDescriptor> responseClasses = new HashMap<String, ClassDescriptor>();

		for (Endpoint endpoint: endpoints) {
			ClassDescriptor classDescriptor = this.javadoc.getClassDescriptor(endpoint.getReturnType());
			processClass(classDescriptor, responseClasses);
		}

		return new Documentation(endpoints, responseClasses);
	}

	private void processClass(ClassDescriptor classDescriptor, Map<String, ClassDescriptor> responseClasses) {
		if (classDescriptor != null && !responseClasses.containsKey(classDescriptor.getName())) {
			responseClasses.put(classDescriptor.getName(),  classDescriptor);
			for (MethodDescriptor methodDescriptor: classDescriptor.getMethodDescriptors()) {
				ClassDescriptor returnTypeDescriptor = this.javadoc.getClassDescriptor(methodDescriptor.getReturnType());
				if (returnTypeDescriptor != null) {
					processClass(returnTypeDescriptor, responseClasses);
				}
                //warbaby 处理泛型
                String genericType = methodDescriptor.getGenericType();
                if(genericType!=null) {
                    ClassDescriptor genericTypeDescriptor = this.javadoc.getClassDescriptor(genericType);
                    if (genericTypeDescriptor != null) {
                        processClass(genericTypeDescriptor, responseClasses);
                    }
                }
			}

            //用不用循环field?
		}
	}
}
