package edu.miu.cs.acs.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UnsureApiInfo extends ApiInfo implements Serializable {
    private String extraInfo;
}
