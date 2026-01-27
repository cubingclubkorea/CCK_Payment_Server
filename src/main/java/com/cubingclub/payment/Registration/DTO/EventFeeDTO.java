package com.cubingclub.payment.Registration.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventFeeDTO {
    private Integer idx;
    private String eventName;   // CubeEvent 기본키
    private Integer eventFee;
}
