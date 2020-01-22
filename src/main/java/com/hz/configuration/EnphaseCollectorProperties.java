package com.hz.configuration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Created by David on 22-Oct-17.
 */
@ConfigurationProperties("envoy")
@Data
@Log4j2
public class EnphaseCollectorProperties {
    private ProtectedHTTPResource controller;
    private int refreshSeconds;     // Misnamed should be refreshMicroSeconds
    private double paymentPerKiloWatt;
    private double chargePerKiloWatt;
    private double dailySupplyCharge;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveRateDate;
    private HTTPResource influxdbResource;
    private PvOutputResource pvOutputResource;

    // Assumes refreshSeconds is a multiple of 60000
    public int getRefreshAsMinutes() {
        return refreshSeconds / 60000;
    }

    @Data
    @NoArgsConstructor
    public static class HTTPResource {
        private String host;
        private int port;
        private String context;

        public String getUrl() {
            if (port == 80) {
                return "http://" + host + (context == null || context.isEmpty() ? "" : "/" + context);
            }
            if (port == 443) {
                return "https://" + host + (context == null || context.isEmpty() ? "" : "/" + context);
            }
            return "http://" + host + ":" + port + (context == null || context.isEmpty() ? "" : "/" + context);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class ProtectedHTTPResource extends HTTPResource {
        private String user;
        private String password;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class PvOutputResource extends HTTPResource {
        private String key;
        private String systemId;
    }
}
