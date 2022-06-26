package com.hz.ui;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.time.LocalDate;

import static com.codeborne.selenide.SetValueOptions.withDate;

// page_url = http://localhost:8080/solar/
public class QnAForm {

    @FindBy(how=How.CSS, using="input[id='dateRange.from']")
    private SelenideElement fromDate;

    @FindBy(css = "input[id='dateRange.to']")
    private SelenideElement toDate;

    @FindBy(css = "#bill-button")
    private SelenideElement answerButton;

    public void setFromDate(LocalDate fromDate) {
        this.fromDate.setValue(withDate(fromDate));
    }

    public void setToDate(LocalDate fromDate) {
        this.toDate.setValue(withDate(fromDate));
    }

    public void clearInputs() {
        this.fromDate.clear();
        this.toDate.clear();
    }

    public void requestAnswer() {
        answerButton.click();
    }

}