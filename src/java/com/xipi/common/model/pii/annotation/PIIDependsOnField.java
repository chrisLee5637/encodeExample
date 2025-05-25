package java.com.xipi.common.model.pii.annotation;

import java.com.xipi.common.model.pii.enumeration.PIIDependsOnFieldType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PIIDependsOnField {
    PIIDependsOnFieldType type(); // The type of field that this PII depends on, e.g., CUSTOMER_ID, CUSTOMER_NAME, etc.
}
