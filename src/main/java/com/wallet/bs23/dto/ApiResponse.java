package com.wallet.bs23.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse implements Serializable {
    static final long serialVersionUID = 1L;

    String message;
    String status;
    String code;

}
