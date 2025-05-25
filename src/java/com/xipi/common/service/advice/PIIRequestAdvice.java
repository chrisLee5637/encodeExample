package java.com.xipi.common.service.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import  org.apache.commons.lang3.reflect.FieldUtils;
import java.com.xipi.common.model.pii.annotation.PIIField;
import java.com.xipi.common.model.pii.enumeration.PIIDatalevel;
import java.com.xipi.common.model.pii.enumeration.PIIFieldType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

@Slf4j
@ControllerAdvice
public class PIIRequestAdvice extends BasePIIAdvice implements RequestBodyAdvice {
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return super.isEncodingApplicable(methodParameter, PIIDatalevel.RESPONSE);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return inputMessage;
    }

    /**
     * if method of supports() returns true, this method will be called to remove user id from request body
     * @param body
     * @param inputMessage
     * @param parameter
     * @param targetType
     * @param converterType
     * @return
     */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        removeCustomerIdFromRequest(body);
        return body;
    }

    private void removeCustomerIdFromRequest(Object body) {
        if (body != null) {
            if (Collection.class.isAssignableFrom(body.getClass())) {
                Collection<?> collection = (Collection<?>) body;
                processCollection(collection);
            } else {
                processClass(body);
            }
        }
    }

    private void processField(Object data, Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(data);
            if (needRemoveCustomerId(field, value)) {
                removeCustomerIdFromContent(data, field, value);
                return;
            }
            if (value !=null) {
                if (isCustomClass(value.getClass(), field)) {
                    processClass(value);
                } else if (Collection.class.isAssignableFrom(value.getClass())) {
                    processCollection((Collection<?>) value);
                }
            }
        } catch (IllegalAccessException e) {
            // Handle the exception as needed, e.g., log it
        }
    }

    private void removeCustomerIdFromContent(Object data, Field field, Object value) {
        try {
            String valStr = (String) value;
            FieldUtils.writeDeclaredField(data, field.getName(), valStr.substring(0, valStr.indexOf('_')), true);
        } catch (IllegalAccessException e) {
            log.error("Failed to remove customer ID from field<{}> from object <{}>: " + field.getName(), data.getClass().getName(), e);
        }
    }

    private boolean needRemoveCustomerId(Field field, Object value) {
        if (field.getAnnotation(PIIField.class) == null) {
            return false;
        }
        return PIIFieldType.isFieldTypeNeedRemoveCustomerId(field.getAnnotation(PIIField.class).type())
                && value != null && ((String) value).contains("_");
    }

    private void processClass(Object body) {
        Field[] declaredFields = getAllFieldsInCurrentOrParentClass(body.getClass());
        for (Field field : declaredFields) {
            processField(body, field);
        }
    }

    private void processCollection(Collection<?> collection) {
        for (Object item : collection) {
            if(isNotInExcludePackages(item.getClass())) {
                processClass(item);
            }
        }
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
