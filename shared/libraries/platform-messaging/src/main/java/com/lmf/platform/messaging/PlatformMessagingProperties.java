package com.lmf.platform.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.outbox")
public class PlatformMessagingProperties {

    /** Intervalo do relay do outbox, em ms. */
    private long pollIntervalMs = 5000;

    /** Tópico de destino quando um evento do outbox esgota as retentativas. */
    private String dltTopic = "platform.outbox.dlt";

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public String getDltTopic() {
        return dltTopic;
    }

    public void setDltTopic(String dltTopic) {
        this.dltTopic = dltTopic;
    }
}
