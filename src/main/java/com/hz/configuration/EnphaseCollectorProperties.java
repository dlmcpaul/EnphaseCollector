package com.hz.configuration;

import com.hz.utils.Calculators;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.util.UriComponentsBuilder;

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
    private MqttResource mqttResource;

    // V7 Autofetch configuration
    private String enphaseWebUser;
    private String enphaseWebPassword;
    private String publicKey;

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
    public static class Resource {
        protected boolean isPropertySet(String property) {
            return (property == null || property.isEmpty()) == false;
        }

        protected boolean isPropertyEmpty(String property) {
            return (property == null || property.isEmpty());
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class HTTPResource extends Resource {
        private String host;
        private int port;
        private String context;

        protected String buildUrl(String scheme, String host, int port, String context) {

            UriComponentsBuilder builder = UriComponentsBuilder.newInstance().scheme(scheme).host(host);

            if (port != 80 && port != 443) {
                builder = builder.port(port);
            }

            if (context == null || context.isEmpty()) {
                return builder.build().toUriString();
            }
            return builder.path("/{context}")
                    .buildAndExpand(context).toUriString();
        }

        public String getUnencryptedUrl() {
            return buildUrl("http", this.host, this.port, this.context);
        }

        public String getEncryptedUrl() {
            return buildUrl("https", this.host, this.port, this.context);
        }

        public String getUrl() {
            if (port == 80) {
                return getUnencryptedUrl();
            }
            if (port == 443) {
                return getEncryptedUrl();
            }

            // Unknown port.  Should we treat this as encrypted?
            return buildUrl("http", this.host, this.port, this.context);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class ProtectedHTTPResource extends HTTPResource {
        private String user;
        private String password;
        private String token;

        public boolean isUserSet() { return isPropertySet(this.user); }
        public boolean isPasswordSet() { return isPropertySet(this.password); }
        public boolean isUserPasswordSet() {
            return (isPropertySet(this.user) && isPropertySet(this.password));
        }
        public boolean isTokenSet() {
            return isPropertySet(token);
        }

        public boolean noAuthenticationSet() {
            return isUserEmpty() && isPasswordEmpty() && isTokenEmpty();
        }

        public boolean isTokenEmpty() {
            return isPropertyEmpty(this.token);
        }
        public boolean isUserEmpty() {
            return isPropertyEmpty(this.user);
        }
        public boolean isPasswordEmpty() {
            return isPropertyEmpty(this.password);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class MqttResource extends ProtectedHTTPResource {
        private String topic;
        private String publisherId;

        @Override
        public String getUrl() {
            return "tcp://" + this.getHost() + ":" + this.getPort();
        }

        public boolean isPublisherIdEmpty() {
            return isPropertyEmpty(this.publisherId);
        }
        public boolean isTopicEmpty() {
            return isPropertyEmpty(this.topic);
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
