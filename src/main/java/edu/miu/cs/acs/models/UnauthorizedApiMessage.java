package edu.miu.cs.acs.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnauthorizedApiMessage implements CheckedAPIMessage{
    private ApiTestStatus type;
    private String apiUrl;
}
