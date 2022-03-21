package com.hz.ui;

import com.codeborne.selenide.Configuration;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
class FirstTabTestsFireFoxBrowser {

	@LocalServerPort //to inject port value
	int port;

	@BeforeAll
	public static void initBrowser() {
		Configuration.browser = "firefox";
		Configuration.headless = true;
	}

	@Test
	void titleIsSet() {
		open("http://localhost:" + port + "/solar");
		$("h1.title").shouldHave(text("Enphase Solar System Visualiser"));
	}

	@Test
	void TabNavigation() {
		open("http://localhost:" + port + "/solar");
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
