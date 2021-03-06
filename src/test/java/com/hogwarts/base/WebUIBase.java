package com.hogwarts.base;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.*;
import java.util.Properties;

public abstract class WebUIBase {
    private Logger logger = Logger.getLogger(WebUIBase.class);
    private String propFileName = "iselenium.properties";

    protected String testcaseName = "";
    //默认浏览器是firefox,可以通过设置程序运行时的环境变量参数currentBrowser进行设置其他
    protected String curBrowser = "firefox";
    protected WebDriver driver;
    protected WebDriver.Navigation navigation;
    protected String firefoxDriverPath = "";
    protected String chromeDriverPath = "";
    protected String browser = "";

    protected int waitTime = 15;

    @BeforeEach
    public void begin() {
        //加载配置文件，注意需要事先将配置文件放到user.home下
        logger.info("Load properties file:" + propFileName);
        Properties prop = loadFromEnvProperties(propFileName);

        //获取浏览器driver路径
        logger.info("Load webdriver path");
        firefoxDriverPath = prop.getProperty("FIREFOX_PATH");
        chromeDriverPath = prop.getProperty("CHROME_PATH");
        browser = prop.getProperty("BROWSER_TYPE");

        logger.info("firefoxDriverPath = " + firefoxDriverPath);
        logger.info("chromeDriverPath = " + chromeDriverPath);
        logger.info("browser = " + browser);

        //设定当前运行的浏览器
        //需要在环境变量"currentBrowser"中配置当前运行什么浏览器, 可选值"firefox","chrome","nogui"
//        setCurBrowser(); //暂时废弃从环境变量中获取的方式，改为从配置文件读取浏览器类型
        curBrowser = browser;
        logger.info("Current browser is " + curBrowser);

        //构造webdriver
        if (curBrowser.equalsIgnoreCase("firefox")) {
            System.setProperty("webdriver.firefox.bin", firefoxDriverPath);
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
            driver = new FirefoxDriver();
        } else if (curBrowser.equalsIgnoreCase("chrome")) {
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            driver = new ChromeDriver();
        } else if (curBrowser.equalsIgnoreCase("nogui")) {
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            driver = new ChromeDriver(chromeOptions);
        } else {
            System.setProperty("webdriver.firefox.bin", firefoxDriverPath);
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
            driver = new FirefoxDriver();
        }

        WebDriver.Timeouts timeout = driver.manage().timeouts();
        timeout.setScriptTimeout(waitTime, java.util.concurrent.TimeUnit.SECONDS);
        timeout.pageLoadTimeout(waitTime, java.util.concurrent.TimeUnit.SECONDS);
        timeout.implicitlyWait(waitTime, java.util.concurrent.TimeUnit.SECONDS);

        navigation = driver.navigate();
    }

    @AfterEach
    public void tearDown() {
        logger.info("Automation test " + testcaseName + " finish!");

        if (driver == null) {
            return;
        }

        driver.quit();
    }

    //加载配置文件
    private Properties loadFromEnvProperties(String propFileName) {
        Properties prop = null;

        String path = System.getProperty("user.home");//获取程序运行环境上用户主目录路径
        logger.info("user.home is " + path);

        //读入envProperties属性文件
        try {
            prop = new Properties();
            InputStream in = new BufferedInputStream(
                    new FileInputStream(path + File.separator + propFileName));
            prop.load(in);
            in.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
            logger.error("Load config file fail, please check " + path + " to confirm if the "
                    + propFileName + " file exist!");
        }

        return prop;
    }

    private void setCurBrowser() {
        String value = System.getenv("currentBrowser");
        logger.info("currentBrowser is " + value);
        if (value == null || value.equalsIgnoreCase("")) {
            return;
        }

        if (value.equalsIgnoreCase("firefox") || value.equalsIgnoreCase("chrome")
                || value.equalsIgnoreCase("nogui")) {
            curBrowser = value.toLowerCase();
        }
    }


    protected void wait2s() {
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {

        }
    }
}
