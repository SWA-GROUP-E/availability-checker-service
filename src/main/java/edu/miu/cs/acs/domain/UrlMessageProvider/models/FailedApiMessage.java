package edu.miu.cs.acs.domain.UrlMessageProvider.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class FailedApiMessage implements CheckedAPIMessage{
    private ApiTestStatus type;
    private String ApiUrl;
}