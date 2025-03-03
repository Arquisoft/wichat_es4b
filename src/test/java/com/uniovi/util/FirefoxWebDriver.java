package com.uniovi.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class FirefoxWebDriver  {
    private static final WebDriver webdriver;

    static {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();

        if ("true".equalsIgnoreCase(System.getenv("headless"))) {
            options.addArguments("--headless");
        }

        webdriver = new FirefoxDriver(options);
    }

    public static WebDriver getDriver() {
        return webdriver;
    }
}
