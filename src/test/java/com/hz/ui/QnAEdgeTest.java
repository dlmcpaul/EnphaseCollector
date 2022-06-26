package com.hz.ui;

import com.codeborne.selenide.Configuration;
import com.hz.configuration.TestEnphaseSystemInfoConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

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

		qnaForm = open("http://localhost:" + port + "/solar", QnAForm.class);
		$("#qna").click();
	}

	@BeforeEach
	void reset() {
		qnaForm.clearInputs();
	}

	@Test
	void testErrorIfFromDateInFuture() {
		LocalDate todayPlusOne = LocalDate.now().plusDays(1);

		log.info("Validating from Date can be set to tomorrow {}", todayPlusOne);
		qnaForm.setFromDate(todayPlusOne);

		$("input[id='dateRange.from']").shouldHave(value(todayPlusOne.toString()));

		qnaForm.requestAnswer();

		// Test for error message
		$("span[id='dateRange.from-icon-danger']").shouldHave(cssClass("has-text-danger"));
		$("p[id='dateRange.from-error-message']").shouldHave(text("Date must be in the past"));
	}

	@AfterAll
	static void shutdown() {
		closeWindow();
	}

}
