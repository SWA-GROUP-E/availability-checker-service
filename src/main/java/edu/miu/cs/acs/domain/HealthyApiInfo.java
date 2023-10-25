package edu.miu.cs.acs.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HealthyApiInfo extends ApiInfo implements Serializable {
    private boolean needsKey;
    private String apiKey;
}
