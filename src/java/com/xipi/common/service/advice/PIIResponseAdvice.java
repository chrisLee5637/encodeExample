package java.com.xipi.common.service.advice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletResponse;
import java.com.xipi.common.model.pii.annotation.PIIDependsOnField;
import java.com.xipi.common.model.pii.annotation.PIIField;
import java.com.xipi.common.model.pii.enumeration.PIIDatalevel;
import java.com.xipi.common.model.pii.enumeration.PIIDependsOnFieldType;
import java.com.xipi.common.model.pii.enumeration.PIIFieldType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class PIIResponseAdvice extends BasePIIAdvice implements ResponseBodyAdvice<Object> {


    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        return super.isEncodingApplicable(methodParameter, PIIDatalevel.REQUEST);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> converter, ServerHttpRequest request, ServerHttpResponse httpResponse) {
        try {
            ServletServerHttpResponse response = (ServletServerHttpResponse) httpResponse;
            HttpServletResponse servletResponse = response.getServletResponse();
            if (servletResponse.getStatus() == HttpStatus.OK.value() && body != null) {
                encodeData(body, null, null);
            } else {
                log.warn("Response status is not OK or body is null, skipping encoding.");
            }
        } catch (Exception e) {
            log.error("Error during response body encoding", e);
        }
        return body;
    }

    private void encodeData(Object body, Map<PIIDependsOnFieldType, String> dependsOnFieldValueMap, Map<Field, Object> piiFieldMap) {
        if (Collection.class.isAssignableFrom(body.getClass())) {
            Collection<?> collection = (Collection<?>) body;
            processCollection(collection);
        } else {
            Field[] declaredFields = getAllFieldsInCurrentOrParentClass(body.getClass());
            if (dependsOnFieldValueMap == null) {
                dependsOnFieldValueMap = new EnumMap<>(PIIDependsOnFieldType.class);
            }
            if (piiFieldMap == null) {
                piiFieldMap = new HashMap<>();
            }
            for (Field field : declaredFields) {
                processField(body, dependsOnFieldValueMap, piiFieldMap, field);
            }
            if (!dependsOnFieldValueMap.isEmpty() && !piiFieldMap.isEmpty()) {
                encodeFieldFromMap(dependsOnFieldValueMap, piiFieldMap);
            }
        }
    }

    private void processField(Object body, Map<PIIDependsOnFieldType, String> dependsOnFieldValueMap, Map<Field, Object> piiFieldMap, Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(body);
            if (field.isAnnotationPresent(PIIDependsOnField.class)) {
                dependsOnFieldValueMap.put(field.getAnnotation(PIIDependsOnField.class).type(), String.valueOf(value));
            }
            if (field.isAnnotationPresent(PIIField.class)) {
                piiFieldMap.put(field, body);
            }
            if (value != null) {
                if (isCustomClass(value.getClass(), field)) {
                    processCustomClass(value, dependsOnFieldValueMap, piiFieldMap);
                } else if (Collection.class.isAssignableFrom(value.getClass())) {
                    processCollection((Collection<?>) value);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("Error accessing field: {}", field.getName(), e);
        }
    }

    private void processCustomClass(Object value, Map<PIIDependsOnFieldType, String> dependsOnFieldValueMap, Map<Field, Object> piiFieldMap) {
        Field[] declareFields = getAllFieldsInCurrentOrParentClass(value.getClass());
        for (Field field : declareFields) {
           processField(value, dependsOnFieldValueMap, piiFieldMap, field);
        }
    }

    private void processCollection(Collection<?> collection) {
        for (Object item : collection) {
            if (isNotInExcludePackages(item.getClass())) {
                encodeData(item, null, null);
            }
        }
    }

    private void encodeFieldFromMap(Map<PIIDependsOnFieldType, String> dependsOnFieldValueMap, Map<Field, Object> piiFieldMap) {
        // Implement the logic to encode fields based on the map values
        // This is a placeholder for the actual encoding logic
        for (Map.Entry<Field, Object> item : piiFieldMap.entrySet()) {
            Field field = item.getKey();
            PIIFieldType piiFieldType = field.getAnnotation(PIIField.class).type();
            try {
                String userId = dependsOnFieldValueMap.get(PIIDependsOnFieldType.CUSTOMER_ID);
                String piiFieldOriginalValue = (String) FieldUtils.readDeclaredField(item.getValue(), field.getName(), true);
                String userNm = dependsOnFieldValueMap.get(PIIDependsOnFieldType.CUSTOMER_NAME);
                switch (piiFieldType) {
                    case CUSTOMER_ID -> applyUseIdRule(item, piiFieldOriginalValue, userId, field);
                    case CUSTOMER_EMAIL -> applyUseMailRule(item, userId, userNm, field);
                    default -> log.warn("Unsupported PIIFieldType: {}", piiFieldType);
                }
            } catch (IllegalAccessException e) {
                log.error("Failed to read field <{}> from object <{}>: {}", field.getName(), item.getValue().getClass().getName(), e.getMessage());
            }
        }
    }

    private void applyUseMailRule(Map.Entry<Field, Object> item, String userNm, String userId, Field field) throws IllegalAccessException {
        if(StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(userNm)) {
            String encodeUserMail = getEncodedUserMailValue(userNm, userId);
            FieldUtils.writeDeclaredField(item.getValue(), field.getName(), encodeUserMail, true);
        }
    }


    private void applyUseIdRule(Map.Entry<Field, Object> item, String piiFieldOriginalValue, String userNm, Field field) throws IllegalAccessException {
        if(StringUtils.isNotEmpty(userNm) && StringUtils.isNotEmpty(piiFieldOriginalValue)) {
            String encodeUserId = getEncodedUserIdValue(piiFieldOriginalValue, userNm);
            FieldUtils.writeDeclaredField(item.getValue(), field.getName(), encodeUserId, true);
        }
    }

    private String getEncodedUserIdValue(String originalValue, String userId) {
        if (StringUtils.isNotEmpty(originalValue) && StringUtils.isNotEmpty(userId)) {
            return String.format("#P.DEC.BN(%s_%s_1.P#", originalValue, userId);
        }
        return null;
    }
    private String getEncodedUserMailValue(String userNm, String userId) {
        if (StringUtils.isNotEmpty(userNm) && StringUtils.isNotEmpty(userId)) {
            return String.format("#P.CAD.AC(%s_%s.P#", userNm, userId);
        }
        return null;
    }
}
