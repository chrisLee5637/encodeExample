package java.com.xipi.common.model.po;

import lombok.Getter;
import lombok.Setter;

import java.com.xipi.common.model.pii.annotation.PIIDependsOnField;
import java.com.xipi.common.model.pii.annotation.PIIField;
import java.com.xipi.common.model.pii.enumeration.PIIDependsOnFieldType;
import java.com.xipi.common.model.pii.enumeration.PIIFieldType;

@Setter
@Getter
public class CustomerDetailsPO {
    @PIIField(type = PIIFieldType.CUSTOMER_ID)
    @PIIDependsOnField(type = PIIDependsOnFieldType.CUSTOMER_ID)
    private String customerId;
    @PIIDependsOnField(type = PIIDependsOnFieldType.CUSTOMER_NAME)
    private String customerName;
    @PIIField(type = PIIFieldType.CUSTOMER_EMAIL)
    private String customerEmail;
    private String customerPhone;

}
