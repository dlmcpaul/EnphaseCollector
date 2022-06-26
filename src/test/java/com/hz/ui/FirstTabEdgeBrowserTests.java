package com.hz.ui;

import com.codeborne.selenide.Configuration;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
class FirstTabEdgeBrowserTests {

	@LocalServerPort //to inject port value
	int port;

	@BeforeAll
	static void initBrowser() {
		Configuration.browser = "edge";
		Configuration.headless = true;
	}

	@BeforeEach
	void reset() {
		open("http://localhost:" + port + "/solar");
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

	@AfterAll
	static void shutdown() {
		closeWindow();
	}

}
