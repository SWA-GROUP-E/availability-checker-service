package edu.miu.cs.acs.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiInfo implements Serializable {
    private String url;
    private String apiKey;
}
