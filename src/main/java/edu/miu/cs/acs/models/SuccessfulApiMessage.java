package edu.miu.cs.acs.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SuccessfulApiMessage implements CheckedAPIMessage{
    private ApiTestStatus type;
    private String apiUrl;
    private String apiKey;
}
