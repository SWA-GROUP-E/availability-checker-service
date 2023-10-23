package edu.miu.cs.acs.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnsureApiInfo extends ApiInfo implements Serializable {
    private String extraInfo;

    public UnsureApiInfo(String url, String extraInfo) {
        super(url);
        this.extraInfo = extraInfo;
    }
}
