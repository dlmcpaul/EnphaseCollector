package com.hz.configuration;

import com.hz.utils.Calculators;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private String bearerToken;     // V7 user authentication configuration
    private int exportLimit = 0;
    private final List<Bands> bands = new ArrayList<>();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveRateDate;
    private ProtectedHTTPResource influxdbResource;
    private PvOutputResource pvOutputResource;
    private MqqtResource mqqtResource;

    // V7 Autofetch configuration
    private String enphaseWebUser;
    private String enphaseWebPassword;

    public int getRefreshSeconds() {
        // Try to handle passing refreshSeconds as named instead of as microseconds
        if (refreshSeconds <= 120) {
            return refreshSeconds * 1000;
        }
        return refreshSeconds;
    }

    public BigDecimal getRefreshAsMinutes() {
        return Calculators.calculateMinutesOfOperation(this.getRefreshSeconds());
    }

    public BigDecimal getRefreshAsMinutes(BigDecimal preferred) {
        return (preferred.compareTo(BigDecimal.ZERO) == 0) ? this.getRefreshAsMinutes() : preferred;
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
        private String token;

        public boolean isUserPasswordSet() {
            return (isUserEmpty() == false && isPasswordEmpty() == false);
        }

        public boolean isTokenSet() {
            return isTokenEmpty() == false;
        }

        public boolean noAuthenticationSet() {
            return isUserEmpty() && isPasswordEmpty() && isTokenEmpty();
        }

        public boolean isTokenEmpty() {
            return (token == null || token.isEmpty());
        }

        public boolean isUserEmpty() {
            return (user == null || user.isEmpty());
        }
        public boolean isPasswordEmpty() {
            return (password == null || password.isEmpty());
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class MqqtResource extends ProtectedHTTPResource {
        private String topic;
        private String publisherId;

        @Override
        public String getUrl() {
            return "tcp://" + this.getHost() + ":" + this.getPort();
        }

        public boolean isPublisherIdEmpty() {
            return (publisherId == null || publisherId.isEmpty());
        }
        public boolean isTopicEmpty() {
            return (topic == null || topic.isEmpty());
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class PvOutputResource extends HTTPResource {
        private String key;
        private String systemId;
    }

    @Data
    @NoArgsConstructor
    public static class Bands {
        String from;
        String to;
        String colour;
    }
}
