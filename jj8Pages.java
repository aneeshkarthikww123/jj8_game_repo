package AutomationMerchantPagenation;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

public class jj8Pages {
    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);

        driver.get("https://www.jj8myr.com/en-my");
        Thread.sleep(3000);

        login(driver);
        closePopupIfPresent(driver);

        Thread.sleep(2000);
        driver.findElement(By.xpath("//a[text()=' Slot']")).click();
        Thread.sleep(1000);
        actions.moveToElement(driver.findElement(By.xpath("//a[text()=' Home']"))).perform();
        Thread.sleep(1000);
        js.executeScript("window.scrollBy(0,-200)");

        WebElement providerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='PP']")));
        String providerName = providerBtn.getText();
        System.out.println("\n[Provider] " + providerName);
        providerBtn.click();
        Thread.sleep(2000);

        String originalTab = driver.getWindowHandle();
        int currentPage = 1;
        int consecutiveFailures = 0;

        while (true) {
            List<WebElement> gameNames = driver.findElements(By.xpath("//div[@class='game_container']//div[@class='game_btn_content_text']"));
            int totalGames = gameNames.size();

            System.out.println("\uD83D\uDCC4 Current Page: " + currentPage + " | Games: " + totalGames);

            for (int i = 0; i < totalGames; i++) {
                if (consecutiveFailures >= 5) {
                    System.out.println("\u26A0\uFE0F Too many failures. Stopping...");
                    break;
                }

                try {
                    List<WebElement> buttons = driver.findElements(By.xpath("//div[@class='game_container']//button"));
                    List<WebElement> names = driver.findElements(By.xpath("//div[@class='game_container']//div[@class='game_btn_content_text']"));

                    WebElement gameBtn = buttons.get(i);
                    String gameName = names.get(i).getText();

                    if (!names.get(i).isDisplayed()) continue;

                    Set<String> beforeTabs = driver.getWindowHandles();
                    js.executeScript("arguments[0].click()", gameBtn);
                    Thread.sleep(3000);

                    Set<String> afterTabs = driver.getWindowHandles();
                    boolean failed;

                    if (afterTabs.size() > beforeTabs.size()) {
                        afterTabs.removeAll(beforeTabs);
                        String newTab = afterTabs.iterator().next();
                        driver.switchTo().window(newTab);
                        failed = handleGameValidation(driver, wait, gameName, true);
                        driver.close();
                        driver.switchTo().window(originalTab);
                    } else {
                        failed = handleGameValidation(driver, wait, gameName, false);
                    }

                    consecutiveFailures = failed ? consecutiveFailures + 1 : 0;
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println("\u26A0\uFE0F Exception while processing game at index " + i);
                    js.executeScript("window.scrollBy(0,200)");
                    Thread.sleep(500);
                }

                restoreCurrentPage(driver, wait, js, currentPage);
            }

            if (consecutiveFailures >= 5) break;

            List<WebElement> paginationBtns = driver.findElements(
                By.xpath("//div[@class='p-holder admin-pagination']/button[not(contains(@class,'p-next')) and not(contains(@class,'p-prev'))]")
            );

            if (paginationBtns.size() <= 1) {
                System.out.println("\u2139\uFE0F Only one page detected. Stopping pagination.");
                break;
            }

            WebElement activeBtn = driver.findElement(By.xpath("//div[@class='p-holder admin-pagination']/button[contains(@class,'active')]"));
            int previousPage = Integer.parseInt(activeBtn.getText());

            List<WebElement> nextBtns = driver.findElements(By.xpath("//div[@class='p-holder admin-pagination']/button[contains(@class,'p-next')]"));
            if (!nextBtns.isEmpty() && nextBtns.get(0).isEnabled()) {
                js.executeScript("arguments[0].scrollIntoView(true);", nextBtns.get(0));
                js.executeScript("arguments[0].click();", nextBtns.get(0));
                Thread.sleep(2000);

                WebElement newActiveBtn = driver.findElement(By.xpath("//div[@class='p-holder admin-pagination']/button[contains(@class,'active')]"));
                int newPageNumber = Integer.parseInt(newActiveBtn.getText());

                if (newPageNumber == previousPage) {
                    System.out.println("\u2139\uFE0F Pagination did not advance. Exiting...");
                    break;
                }

                currentPage = newPageNumber;
                System.out.println("\u27A1\uFE0F Moving to page: " + currentPage);
            } else {
                break;
            }
        }

        System.out.println("\n\u2705 Slot game testing completed.");
        driver.quit();
    }

    private static void login(WebDriver driver) throws InterruptedException {
        driver.findElement(By.xpath("//div[@class='flex items-center gap-2 mr-2']//button[text()='Login']")).click();
        Thread.sleep(1000);
        driver.findElement(By.xpath("//input[@placeholder='Enter Your Username']")).sendKeys("600testacc");
        driver.findElement(By.xpath("//input[@placeholder='Password']")).sendKeys("qweqwe11");
        driver.findElement(By.xpath("//div[@class='relative flex justify-center']/button[text()='Login']")).click();
        Thread.sleep(3000);
    }

    private static void closePopupIfPresent(WebDriver driver) {
        try {
            WebElement popupClose = driver.findElement(By.xpath("//div[contains(@class,'fs-overlay')]//img[contains(@src,'close')]"));
            popupClose.click();
        } catch (Exception ignored) {}
    }

    private static void restoreCurrentPage(WebDriver driver, WebDriverWait wait, JavascriptExecutor js, int currentPage) {
        try {
            List<WebElement> paginationButtons = driver.findElements(By.xpath("//div[@class='p-holder admin-pagination']/button[not(contains(@class,'p-next')) and not(contains(@class,'p-prev'))]"));

            if (paginationButtons.size() > 1) {
                WebElement activePage = driver.findElement(By.xpath("//div[@class='p-holder admin-pagination']/button[contains(@class,'active')]"));
                int currentUIPage = Integer.parseInt(activePage.getText());

                if (currentUIPage != currentPage) {
                    WebElement targetPageBtn = driver.findElement(By.xpath("//div[@class='p-holder admin-pagination']/button[text()='" + currentPage + "']"));
                    js.executeScript("arguments[0].scrollIntoView(true);", targetPageBtn);
                    wait.until(ExpectedConditions.elementToBeClickable(targetPageBtn));
                    js.executeScript("arguments[0].click();", targetPageBtn);
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            System.out.println("\u26A0\uFE0F Page restore failed: " + e.getMessage());
        }
    }

    private static boolean handleGameValidation(WebDriver driver, WebDriverWait wait, String gameName, boolean isNewTab) throws InterruptedException {
        boolean failed = false;

        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
            System.out.println("\u274C Alert: GAME DISABLED — " + gameName);
            failed = true;
        } catch (Exception ignored) {}

        if (driver.findElements(By.xpath("//div[text()='Failed to load game']")).stream().anyMatch(WebElement::isDisplayed)) {
            System.out.println("\u274C Load Fail: " + gameName);
            failed = true;
        } else if (!isNewTab && driver.findElements(By.xpath("//*[@style = 'word-wrap: break-word; white-space: pre-wrap;']")).size() > 0) {
            System.out.println("\u274C Error Displayed: " + gameName);
            failed = true;
        } else if (!failed) {
            System.out.println("\u2705 Success: " + gameName);
        }

        if (!isNewTab) {
            try {
                WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@class='flex items-center']/button/*[contains(@class, 'game_header_close_btn')]")));
                closeBtn.click();
            } catch (Exception e) {
                driver.navigate().back();
            }
        }

        if (!isNewTab && failed) {
            try {
                WebElement backBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Back To Home']")));
                backBtn.click();
            } catch (Exception e) {
                driver.navigate().back();
            }
        }

        return failed;
    }
}
