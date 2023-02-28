package com.hz.ui;

import com.codeborne.selenide.WebDriverRunner;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FirstTabFireFoxBrowserTests {

	@Container
	public static BrowserWebDriverContainer<?> firefox = new BrowserWebDriverContainer<>()
			.withCapabilities(new FirefoxOptions().setHeadless(true))
			.withAccessToHost(true);

	@LocalServerPort //to inject port value to non-static field
	int appPort;

	@BeforeAll
	void startup() {
		org.testcontainers.Testcontainers.exposeHostPorts(appPort);
		WebDriverRunner.setWebDriver(new RemoteWebDriver(firefox.getSeleniumAddress(), new FirefoxOptions().setHeadless(true)));
	}

	@BeforeEach
	void reset() {
		open(String.format("http://host.testcontainers.internal:%d/solar", appPort));
	}

	@Test
	void titleIsSet() {
		$("h1.title").shouldHave(text("Enphase Solar System Visualiser"));
	}

	@Test
	void TabNavigation() {
		$("#weekly").click();

		$("#live").shouldNotHave(cssClass("is-active"));
		$("#live-data").shouldHave(cssClass("is-hidden"));

		$("#weekly").shouldHave(cssClass("is-active"));
		$("#weekly-data").shouldNotHave(cssClass("is-hidden"));

		$("#monthly").click();
		$("#monthly").shouldHave(cssClass("is-active"));
		$("#monthly-data").shouldNotHave(cssClass("is-hidden"));

		$("#quarterly").click();
		$("#quarterly").shouldHave(cssClass("is-active"));
		$("#quarterly-data").shouldNotHave(cssClass("is-hidden"));

		$("#qna").click();
		$("#qna").shouldHave(cssClass("is-active"));
		$("#qna-data").shouldNotHave(cssClass("is-hidden"));
	}

}
