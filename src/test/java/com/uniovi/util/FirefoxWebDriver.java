package com.uniovi.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.events.EventFiringWebDriver;

public class FirefoxWebDriver extends EventFiringWebDriver {
    private static final WebDriver webdriver;

    static {
        WebDriverManager.firefoxdriver().setup();
        if (System.getenv("headless") != null && System.getenv("headless").equals("true")) {
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--headless");
            webdriver = new FirefoxDriver(options);
        } else {
            webdriver = new FirefoxDriver();
        }
    }

    public FirefoxWebDriver() {
        super(webdriver);
    }
}
