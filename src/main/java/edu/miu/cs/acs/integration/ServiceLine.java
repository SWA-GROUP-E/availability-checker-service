package edu.miu.cs.acs.integration;

import lombok.Getter;

@Getter
public enum ServiceLine {
    SUCCESSFUL("successful") {
        @Override
        public String getChannel() {
            return Channels.SUCCESSFUL_API_CHANNEL;
        }
    },
    FAILED("failed") {
        @Override
        public String getChannel() {
            return Channels.FAILED_API_CHANNEL;
        }
    },
    UNAUTHORIZED("unauthorized") {
        @Override
        public String getChannel() {
            return Channels.UNAUTHORIZED_API_CHANNEL;
        }
    };

    private String value;

    ServiceLine(String value) {
        this.value = value;
    }

    public abstract String getChannel();
}
