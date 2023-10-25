package edu.miu.cs.acs.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckedApiMessage {
    private ApiTestStatus testStatus;
    private String apiUrl;
    private String apiKey;
}
