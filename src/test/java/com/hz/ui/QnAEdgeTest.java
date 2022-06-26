package com.hz.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Locale;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
@Import(TestEnphaseSystemInfoConfig.class)
class QnAEdgeTest {

	static QnAForm qnaForm;

	@BeforeAll
	static void initBrowser(@Value("${local.server.port}") int port) {
		Configuration.browser = "edge";
		Configuration.headless = true;

		open("http://localhost:" + port + "/solar");
		$("#qna").click();
		qnaForm = new QnAForm(Selenide.webdriver().object());
	}

	@BeforeEach
	void reset() {
		qnaForm.fromDate.clear();
		qnaForm.toDate.clear();
	}

	@Test
	void testErrorIfFromDateInFuture() {
		LocalDate todayPlusOne = LocalDate.now().plusDays(1);
		String todayPlusOneString = String.format(Locale.US, "%02d", todayPlusOne.getDayOfMonth()) + String.format(Locale.US, "%02d", todayPlusOne.getMonthValue()) + todayPlusOne.getYear();

		log.info("Validating from Date can be set to tomorrow {} with keys {}", todayPlusOne, todayPlusOneString);
		qnaForm.fromDate.sendKeys(todayPlusOneString + Keys.TAB);

		$("input[id='dateRange.from']").shouldHave(value(todayPlusOne.toString()));

		qnaForm.answerButton.click();

		// Test for error message
		$("span[id='dateRange.from-icon-danger']").shouldHave(cssClass("has-text-danger"));
		$("p[id='dateRange.from-error-message']").shouldHave(text("Date must be in the past"));
	}

	@AfterAll
	static void shutdown() {
		closeWebDriver();
	}

}
