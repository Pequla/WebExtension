package com.pequla.web.extension.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SparkError {

    private String message;
    private Long timestamp;
}
