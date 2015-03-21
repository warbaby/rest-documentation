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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.rest.documentation.javadoc.ClassDescriptor;
import org.springframework.rest.documentation.javadoc.FieldDescriptor;
import org.springframework.rest.documentation.javadoc.MethodDescriptor;
import org.springframework.rest.documentation.model.Endpoint;
import org.springframework.rest.documentation.model.Outcome;
import org.springframework.rest.documentation.model.Parameter;
import org.springframework.rest.documentation.model.ParameterType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationAllowableListValues;
import com.wordnik.swagger.core.DocumentationAllowableValues;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.DocumentationError;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.DocumentationParameter;
import com.wordnik.swagger.core.DocumentationSchema;

@ConfigurationProperties(name = "endpoints.swagger_documentation", ignoreUnknownFields = false)
public class SwaggerDocumentationEndpoint extends AbstractEndpoint<Documentation> implements
      ApplicationContextAware {

    private RestDocumentationView restDocumentationView = new RestDocumentationView();

    private ObjectMapper objectMapper;

    public SwaggerDocumentationEndpoint() {
        super("swagger_documentation");
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.restDocumentationView.setApplicationContext(context);

        RequestMappingHandlerAdapter bean = context.getBean(RequestMappingHandlerAdapter.class);
        for (HttpMessageConverter<?> converter : bean.getMessageConverters()) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                this.objectMapper = ((MappingJackson2HttpMessageConverter) converter).getObjectMapper();
                break;
            }
        }
    }

    @Override
    public Documentation invoke() {
        org.springframework.rest.documentation.model.Documentation documentation = this.restDocumentationView.getSnapshot();
        Documentation swaggerDocumentation = new Documentation("Unknown", "1.2", "http://localhost:8080", "/");
        Map<String, List<Endpoint>> endpointsByPath = new HashMap<String, List<Endpoint>>();
        for (Endpoint endpoint : documentation.getEndpoints()) {
            List<Endpoint> endpoints = endpointsByPath.get(endpoint.getUriPattern());
            if (endpoints == null) {
                endpoints = new ArrayList<Endpoint>();
                endpointsByPath.put(endpoint.getUriPattern(), endpoints);
            }
            endpoints.add(endpoint);
        }

        int nicknameCounter = 0;
        int operationsCounter = 0;

        Set<String> returnTypes = new HashSet<String>();

        for (Map.Entry<String, List<Endpoint>> entry : endpointsByPath.entrySet()) {
            DocumentationEndPoint documentationEndPoint = new DocumentationEndPoint(entry.getKey(), "Description " + operationsCounter++);
            for (Endpoint endpoint : entry.getValue()) {
                DocumentationOperation documentationOperation = new DocumentationOperation(endpoint.getRequestMethod().name(), endpoint.getSummary(), endpoint.getDescription());
                if (endpoint.getReturnType() != null && !"void".equals(endpoint.getReturnType())) {
                    returnTypes.add(endpoint.getReturnType());
                    documentationOperation.setResponseClass(endpoint.getReturnType().substring(endpoint.getReturnType().lastIndexOf('.') + 1));
                }
                documentationOperation.setNickname(Integer.toString(nicknameCounter++));
                for (Outcome outcome : endpoint.getOutcomes()) {
                    if (outcome.getStatus().value() >= 400) {
                        documentationOperation.addErrorResponse(new DocumentationError(outcome.getStatus().value(), outcome.getDescription()));
                    }
                }
                for (Parameter parameter : endpoint.getParameters()) {
                    DocumentationAllowableValues allowableValues = null;
                    DocumentationParameter documentationParameter = new DocumentationParameter(parameter.getName(), parameter.getDescription(), "", getParamType(parameter), null, allowableValues, parameter.isRequired(), false);
                    documentationParameter.setDataType(getSwaggerDataType(parameter.getType()));
                    documentationOperation.addParameter(documentationParameter);
                }
                documentationEndPoint.addOperation(documentationOperation);
            }
            swaggerDocumentation.addApi(documentationEndPoint);
        }

        addModels(returnTypes, documentation.getResponseClasses(), swaggerDocumentation);

        return swaggerDocumentation;
    }

    private boolean isPrimitiveType(String type) {
        return int.class.getName().equals(type)
              || byte.class.getName().equals(type)
              || short.class.getName().equals(type)
              || long.class.getName().equals(type)
              || float.class.getName().equals(type)
              || double.class.getName().equals(type)
              || char.class.getName().equals(type)
              || boolean.class.getName().equals(type);
    }

    private String getSwaggerDataType(String type) {
        if (Integer.class.getName().equals(type) || int.class.getName().equals(type)) {
            return "integer";
        } else if (Long.class.getName().equals(type) || long.class.getName().equals(type)) {
            return "integer";
        } else if (Short.class.getName().equals(type) || short.class.getName().equals(type)) {
            return "integer";
        } else if (Float.class.getName().equals(type) || float.class.getName().equals(type)) {
            return "float";
        } else if (Double.class.getName().equals(type) || double.class.getName().equals(type)) {
            return "double";
        } else if (String.class.getName().equals(type)) {
            return "string";
        } else if (Character.class.getName().equals(type) || char.class.getName().equals(type)) {
            return "string";
        } else if (Byte.class.getName().equals(type) || byte.class.getName().equals(type)) {
            return "byte";
        } else if (Boolean.class.getName().equals(type) || boolean.class.getName().equals(type)) {
            return "boolean";
        } else if (Date.class.getName().equals(type)) {
            return "date-time";
        } else {
            if (type.lastIndexOf('$') >= 0) {
                return type.substring(type.lastIndexOf('$') + 1);
            } else if (type.lastIndexOf('.') >= 0) {
                return type.substring(type.lastIndexOf('.') + 1);
            } else {
                return type;
            }
        }
    }

    private void addModels(Set<String> returnTypes, Map<String, ClassDescriptor> responseClasses, Documentation documentation) {
        for (String returnType : returnTypes) {
            if (!isPrimitiveType(returnType)) {
                addModelForType(returnType, responseClasses, documentation);
            }
        }
    }

    private void addModelForType(String type, Map<String, ClassDescriptor> responseClasses, Documentation documentation) {
        String name = getSwaggerDataType(type);
        if (documentation.getModels() != null && documentation.getModels().containsKey(name)) return;

        DocumentationSchema schema = new DocumentationSchema();

        ClassDescriptor classDescriptor = responseClasses.get(type);
        if (classDescriptor != null) {
            schema.setDescription(classDescriptor.getName());
        }

        try {
            Class<?> clazz = Class.forName(type);

            if (clazz.isEnum()) {
                Object[] enumConstants = clazz.getEnumConstants();
                List<String> values = new ArrayList<String>();
                for (Object enumConstant : enumConstants) {
                    values.add(enumConstant.toString());
                }
                schema.setAllowableValues(new DocumentationAllowableListValues(values));
            } else if (classDescriptor != null) {
                BasicClassIntrospector introspector = new BasicClassIntrospector();

                Map<String, DocumentationSchema> properties = new HashMap<String, DocumentationSchema>();

                BasicBeanDescription descriptor = introspector.forSerialization(this.objectMapper.getSerializationConfig(), TypeFactory.defaultInstance().constructType(clazz), this.objectMapper.getSerializationConfig());

                for (BeanPropertyDefinition property : descriptor.findProperties()) {
                    String propertyName = property.getName();

                    FieldDescriptor fieldDescriptor = classDescriptor.getFieldDescriptor(propertyName);
                    AnnotatedMember accessor = property.getAccessor();
                    MethodDescriptor methodDescriptor = accessor == null ? null : classDescriptor.getMethodDescriptor((Method) accessor.getAnnotated());
                    if(fieldDescriptor == null && methodDescriptor == null) {
                        System.err.println("No field and method descriptor in javadoc" + propertyName);
                    }

                    //属性上的注释优先, 或者找不到getter或者javadoc里没有getter(lombok)
                    if ( fieldDescriptor!=null && ((fieldDescriptor.getSummary() != null && !fieldDescriptor.getSummary().isEmpty()) || methodDescriptor == null)) {
                        DocumentationSchema propertySchema = getPropertySchema(responseClasses, documentation, fieldDescriptor.getSummary(), fieldDescriptor.getDescription(), fieldDescriptor.getType());
                        properties.put(propertyName, propertySchema);
                    } else if (methodDescriptor != null) {
                        String propertyType = methodDescriptor.getReturnType();
                        DocumentationSchema propertySchema = getPropertySchema(responseClasses, documentation, methodDescriptor.getSummary(), methodDescriptor.getDescription(), propertyType);
                        properties.put(propertyName, propertySchema);
                    }
                }

                schema.setProperties(properties);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        documentation.addModel(name, schema);
    }

    private DocumentationSchema getPropertySchema(Map<String, ClassDescriptor> responseClasses, Documentation documentation, String summary, String description, String propertyType) throws ClassNotFoundException {

        DocumentationSchema propertySchema = new DocumentationSchema();

        //swagger属性的说明只有一个，合并javadoc
        String text = summary;
        if (description != null && !description.trim().isEmpty()) text += "\n" + description;

        propertySchema.setDescription(text);
        String swaggerDataType = getSwaggerDataType(propertyType);
        if (isPrimitiveType(propertyType)) {
            propertySchema.setType(swaggerDataType);
        } else {
            Class<?> propertyClass = Class.forName(propertyType);

            if (propertyClass.isEnum()) {
                Object[] enumConstants = propertyClass.getEnumConstants();
                List<String> values = new ArrayList<String>();
                for (Object enumConstant : enumConstants) {
                    values.add(enumConstant.toString());
                }
                propertySchema.setType("string");
                propertySchema.setAllowableValues(new DocumentationAllowableListValues(values));
            } else {
                propertySchema.setType(swaggerDataType);
                addModelForType(propertyType, responseClasses, documentation);
            }
        }
        return propertySchema;
    }

    private String getParamType(Parameter parameter) {
        ParameterType parameterType = parameter.getParameterType();

        if (parameterType == ParameterType.BODY) {
            return "body";
        } else if (parameterType == ParameterType.REQUEST_PARAMETER) {
            return "query";
        } else if (parameterType == ParameterType.PATH) {
            return "path";
        } else {
            throw new IllegalArgumentException("Parameter has an unsupported type");
        }
    }
}
