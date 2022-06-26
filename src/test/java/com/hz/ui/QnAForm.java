package com.hz.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

// page_url = http://localhost:8080/solar/
public class QnAForm {

    @FindBy(css = "input[id='dateRange.from']")
    public WebElement fromDate;

    @FindBy(css = "input[id='dateRange.to']")
    public WebElement toDate;

    @FindBy(css = "#bill-button")
    public WebElement answerButton;

    public QnAForm(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}