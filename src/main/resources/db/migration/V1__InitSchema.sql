-- H2 2.1.214;
;
CREATE USER IF NOT EXISTS "SA" SALT '83c022e8ce4e4b62' HASH '1ec9b9cb820fee86f38cec79f8ca9560b73e1630da96ee2c0f99e9a5aa86569f' ADMIN;
CREATE SEQUENCE "PUBLIC"."HIBERNATE_SEQUENCE" START WITH 4003352 RESTART WITH 4005137;
CREATE CACHED TABLE "PUBLIC"."ENVOY_SYSTEM"(
                                               "ID" BIGINT SELECTIVITY 100 NOT NULL,
                                               "ENVOY_SERIAL" CHARACTER VARYING(255) SELECTIVITY 100,
                                               "ENVOY_VERSION" CHARACTER VARYING(255) SELECTIVITY 100,
                                               "LAST_COMMUNICATION" TIMESTAMP SELECTIVITY 100,
                                               "LAST_READ_TIME" TIMESTAMP SELECTIVITY 100,
                                               "NETWORK" CHARACTER VARYING(255) SELECTIVITY 100,
                                               "PANEL_COUNT" INTEGER SELECTIVITY 100 NOT NULL,
                                               "WIFI" BOOLEAN SELECTIVITY 100 NOT NULL
);
ALTER TABLE "PUBLIC"."ENVOY_SYSTEM" ADD CONSTRAINT "PUBLIC"."envoy_system_pk" PRIMARY KEY("ID");

CREATE CACHED TABLE "PUBLIC"."EVENT"(
                                        "ID" BIGINT SELECTIVITY 100 NOT NULL,
                                        "CONSUMPTION" DECIMAL(19, 2) SELECTIVITY 98,
                                        "PRODUCTION" DECIMAL(19, 2) SELECTIVITY 62,
                                        "TIME" TIMESTAMP SELECTIVITY 100,
                                        "VOLTAGE" DECIMAL(19, 2) SELECTIVITY 53
);
ALTER TABLE "PUBLIC"."EVENT" ADD CONSTRAINT "PUBLIC"."event_pk" PRIMARY KEY("ID");

CREATE CACHED TABLE "PUBLIC"."EVENT_PANELS"(
                                               "EVENT_ID" BIGINT NOT NULL,
                                               "PANELS_ID" BIGINT NOT NULL
);

CREATE CACHED TABLE "PUBLIC"."PANEL"(
                                        "ID" BIGINT NOT NULL,
                                        "IDENTIFIER" CHARACTER VARYING(255),
                                        "PANEL_VALUE" FLOAT NOT NULL
);
ALTER TABLE "PUBLIC"."PANEL" ADD CONSTRAINT "PUBLIC"."panel_pk" PRIMARY KEY("ID");

CREATE CACHED TABLE "PUBLIC"."SUMMARY"(
                                          "DATE" DATE SELECTIVITY 100 NOT NULL,
                                          "CONSUMPTION" DECIMAL(19, 2) SELECTIVITY 100,
                                          "GRID_EXPORT" DECIMAL(19, 2) SELECTIVITY 97,
                                          "GRID_IMPORT" DECIMAL(19, 2) SELECTIVITY 99,
                                          "PRODUCTION" DECIMAL(19, 2) SELECTIVITY 99,
                                          "HIGHEST_OUTPUT" BIGINT SELECTIVITY 49,
                                          "CONVERSION_RATE" DECIMAL(19, 2) SELECTIVITY 1
);
ALTER TABLE "PUBLIC"."SUMMARY" ADD CONSTRAINT "PUBLIC"."summary_pk" PRIMARY KEY("DATE");

CREATE CACHED TABLE "PUBLIC"."ELECTRICITY_RATE"(
                                                   "EFFECTIVE_DATE" DATE SELECTIVITY 100 NOT NULL,
                                                   "CHARGE_PER_KILO_WATT" DOUBLE PRECISION SELECTIVITY 100,
                                                   "DAILY_SUPPLY_CHARGE" DOUBLE PRECISION SELECTIVITY 100,
                                                   "PAYMENT_PER_KILO_WATT" DOUBLE PRECISION SELECTIVITY 100
);
ALTER TABLE "PUBLIC"."ELECTRICITY_RATE" ADD CONSTRAINT "PUBLIC"."electricity_rate_pk" PRIMARY KEY("EFFECTIVE_DATE");

ALTER TABLE "PUBLIC"."EVENT_PANELS" ADD CONSTRAINT "PUBLIC"."event_panels_ix1" UNIQUE("PANELS_ID");
ALTER TABLE "PUBLIC"."EVENT_PANELS" ADD CONSTRAINT "PUBLIC"."event_panels_fk1" FOREIGN KEY("EVENT_ID") REFERENCES "PUBLIC"."EVENT"("ID") NOCHECK;
ALTER TABLE "PUBLIC"."EVENT_PANELS" ADD CONSTRAINT "PUBLIC"."event_panels_fk2" FOREIGN KEY("PANELS_ID") REFERENCES "PUBLIC"."PANEL"("ID") NOCHECK;
