package java.com.xipi.common.model.pii.annotation;

import java.com.xipi.common.model.pii.enumeration.PIIFieldType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PIIField {
    PIIFieldType type(); // The type of PII field, e.g., CUSTOMER_ID, CUSTOMER_NAME, etc.
}
