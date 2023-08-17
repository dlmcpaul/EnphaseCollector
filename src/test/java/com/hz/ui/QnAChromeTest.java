package com.hz.ui;

import com.codeborne.selenide.WebDriverRunner;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QnAChromeTest {

	@Container
	public static BrowserWebDriverContainer<?> edge = new BrowserWebDriverContainer<>()
			.withCapabilities(new ChromeOptions().addArguments("--headless=new"))
			.withAccessToHost(true);

	static QnAForm qnaForm;

	@LocalServerPort // to inject port value to non-static field
	int appPort;

	@BeforeAll
	void startup() {
		org.testcontainers.Testcontainers.exposeHostPorts(appPort);
		WebDriverRunner.setWebDriver(new RemoteWebDriver(edge.getSeleniumAddress(), new ChromeOptions().addArguments("--headless=new"), false));
		qnaForm = open("http://host.testcontainers.internal:%d/solar".formatted(appPort), QnAForm.class);
		$("#qna").click();
	}

	@BeforeEach
	void reset() {
		qnaForm.clearInputs();
	}

	@Test
	void testErrorIfFromDateInFuture() {
		LocalDate todayPlusOne = LocalDate.now().plusDays(1);

		log.info("Set from Date to tomorrow {}", todayPlusOne);
		qnaForm.setFromDate(todayPlusOne);

		$("input[id='dateRange.from']").shouldHave(value(todayPlusOne.toString()));

		qnaForm.requestAnswer();

		// Test for error message
		$("span[id='dateRange.from-icon-danger']").shouldHave(cssClass("has-text-danger"));
		$("p[id='dateRange.from-error-message']").shouldHave(text("Date must be in the past"));
	}

}
