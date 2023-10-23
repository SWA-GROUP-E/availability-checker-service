package edu.miu.cs.acs.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthyApiInfo extends ApiInfo implements Serializable {
    private boolean needsKey;
    private String apiKey;

    public HealthyApiInfo(String url, boolean needsKey, String apiKey) {
        super(url);
        this.needsKey = needsKey;
        this.apiKey = apiKey;
    }
}
