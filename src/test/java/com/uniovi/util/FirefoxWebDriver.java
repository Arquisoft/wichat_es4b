package com.uniovi.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.openqa.selenium.support.events.WebDriverListener;

public class FirefoxWebDriver {
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

    public static WebDriver getWebDriver(WebDriverListener listener) {
        return new EventFiringDecorator(listener).decorate(webdriver);
    }
}