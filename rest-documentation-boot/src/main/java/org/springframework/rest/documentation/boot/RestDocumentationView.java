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

package org.springframework.rest.documentation.boot;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.rest.documentation.DocumentationGenerator;
import org.springframework.rest.documentation.javadoc.Javadoc;
import org.springframework.rest.documentation.model.Documentation;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class RestDocumentationView implements ApplicationContextAware {
	
	
	private volatile DocumentationGenerator documentationGenerator;
	private Resource resource;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {

		resource = applicationContext.getResource("classpath:javadoc.json");
		this.documentationGenerator = new DocumentationGenerator(applicationContext);
	}
	
	public synchronized Documentation getSnapshot() {
		try {
			InputStream inputStream = resource.getInputStream();
			return this.documentationGenerator.generate(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
