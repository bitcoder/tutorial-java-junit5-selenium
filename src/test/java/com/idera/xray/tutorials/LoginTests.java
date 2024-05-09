package com.idera.xray.tutorials;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import app.getxray.xray.junit.customjunitxml.XrayTestReporter;
import app.getxray.xray.junit.customjunitxml.XrayTestReporterParameterResolver;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import app.getxray.xray.junit.customjunitxml.annotations.XrayTest;

@ExtendWith(XrayTestReporterParameterResolver.class)
class LoginTests {
    WebDriver driver;
    RepositoryParser repo;

    @BeforeEach
    public void setUp() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox"); // Bypass OS security model, to run in Docker
        options.addArguments("--headless");
	options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        repo = new RepositoryParser("./src/configs/object.properties");
    }

    @AfterEach
    public void tearDown() throws Exception {
        driver.quit();
        driver = null;
        repo = null;
    }
    

    /*
     * this is a typical test implement in JUnit
     * a new Generic Test will be created in Xray, unless one already exists for the same package name + method name
     */
    @Test
    void loginPageOpens()
    {
        LoginPage loginPage = new LoginPage(driver).open();
        assertTrue(loginPage.isVisible());
    }


    /*
     * this test will report results against an existing Test in Xray
     * it will also link the test to a requirement/story in Jira
     */
    @Test
    @XrayTest(key = "CALC-2702")
    @Requirement("CALC-2703")
    void successLogin()
    {
        LoginPage loginPage = new LoginPage(driver).open();
        assertTrue(loginPage.isVisible());
        LoginResultsPage loginResultsPage = loginPage.login("demo", "mode");
        assertEquals(loginResultsPage.getTitle(), repo.getBy("expected.login.title"));
        assertTrue(loginResultsPage.contains(repo.getBy("expected.login.success")));
    }

    /*
     * this test will report results for a new Test that will be created in Xray, unless it already exists with the same summary
     */
    @Test
    @XrayTest(summary = "invalid login test", description = "login attempt with invalid credentials")
    void nosuccessLogin(XrayTestReporter xrayReporter)
    {
        LoginPage loginPage = new LoginPage(driver).open();
        assertTrue(loginPage.isVisible());
        LoginResultsPage loginResultsPage = loginPage.login("demo", "invalid");
        TakesScreenshot screenshotTaker =((TakesScreenshot)driver);
        File screenshot = screenshotTaker.getScreenshotAs(OutputType.FILE);
        xrayReporter.addTestRunEvidence(screenshot.getAbsolutePath());
        xrayReporter.addComment("auth should have failed");
        assertEquals(loginResultsPage.getTitle(), repo.getBy("expected.error.title"));
        assertTrue(loginResultsPage.contains(repo.getBy("expected.login.failed")));
    }

}
