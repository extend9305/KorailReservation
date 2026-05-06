import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class Main {

    // ===================== 설정값 (여기만 수정) =====================
    private static final String USER_ID   = "";         // 코레일 회원 아이디
    private static final String USER_PWD  = "";         // 코레일 비밀번호
    private static final String DEPARTURE = "영등포";   // 출발역
    private static final String ARRIVAL   = "대전";     // 도착역
    private static final String YEAR      = "2026";
    private static final String MONTH     = "05";       // MM
    private static final String DAY       = "21";       // DD
    private static final String HOUR      = "11";       // 출발 시간 (00~23)
    private static final int    RETRY_INTERVAL_MS = 3000; // 재시도 간격 (밀리초)
    // ==============================================================

    private static final String LOGIN_URL  = "https://www.korail.com/";
    private static final String SEARCH_URL = "https://www.korail.com/ticket/search/list";

    private final WebDriver    driver;
    private final WebDriverWait wait;

    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.login();
            main.searchAndMonitor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Main() {
        // Windows용 chromedriver 경로 (필요시 수정)
        System.setProperty("webdriver.chrome.driver", "etc/chromedriver_win32/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new"); // 백그라운드 실행 시 주석 해제
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    /** 코레일 로그인 */
    private void login() throws InterruptedException {
        driver.get(LOGIN_URL);
        Thread.sleep(2000);

        // 로그인 버튼(헤더) 클릭 → 로그인 페이지 이동
        // TODO: F12 > Elements에서 '로그인' 링크 셀렉터 확인 후 수정
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'로그인')] | //button[contains(text(),'로그인')]")));
        loginLink.click();
        Thread.sleep(2000);

        // 아이디 입력
        // TODO: F12 > Elements에서 아이디 input의 id/name 확인 후 수정
        WebElement idField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='memberId'], input[id='memberId'], input[name='usrId'], input[id='usrId']")));
        idField.clear();
        idField.sendKeys(USER_ID);
        Thread.sleep(300);

        // 비밀번호 입력
        // TODO: F12 > Elements에서 비밀번호 input의 id/name 확인 후 수정
        WebElement pwdField = driver.findElement(
                By.cssSelector("input[type='password'][name='memberPw'], input[type='password'][id='memberPw'], input[type='password']"));
        pwdField.clear();
        pwdField.sendKeys(USER_PWD);
        Thread.sleep(300);

        // 로그인 버튼 클릭
        WebElement loginBtn = driver.findElement(
                By.cssSelector("button[type='submit'], .btn_login, #loginBtn"));
        loginBtn.click();
        Thread.sleep(3000);

        System.out.println("로그인 완료");
    }

    /** 승차권 조회 후 취소표 모니터링 */
    private void searchAndMonitor() throws InterruptedException {
        driver.get(SEARCH_URL);
        Thread.sleep(2000);

        fillSearchForm();
        clickSearchButton();

        System.out.println("취소표 모니터링 시작...");
        monitorAndReserve();
    }

    /** 조회 폼 입력 */
    private void fillSearchForm() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 출발역
        // TODO: F12에서 출발역 input의 id 확인 (예: txtGoStart, dptRsStnNm 등)
        WebElement depField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#txtGoStart, input[placeholder*='출발역'], input[placeholder*='출발']")));
        depField.clear();
        depField.sendKeys(DEPARTURE);
        Thread.sleep(500);
        // 자동완성 팝업에서 첫 번째 항목 선택
        selectFirstAutoComplete();

        // 도착역
        WebElement arrField = driver.findElement(
                By.cssSelector("#txtGoEnd, input[placeholder*='도착역'], input[placeholder*='도착']"));
        arrField.clear();
        arrField.sendKeys(ARRIVAL);
        Thread.sleep(500);
        selectFirstAutoComplete();

        // 날짜 설정 (JavaScript로 직접 value 주입 후 change 이벤트 발생)
        // TODO: F12에서 날짜 input의 id 확인
        String dateValue = YEAR + MONTH + DAY;
        js.executeScript(
            "var el = document.querySelector('#dtGoStart, input[name*=\"date\"], input[id*=\"date\"]');" +
            "if(el){ el.value='" + dateValue + "'; el.dispatchEvent(new Event('change', {bubbles:true})); }"
        );
        Thread.sleep(300);

        // 시간 설정
        // TODO: F12에서 시간 select/input의 id 확인
        js.executeScript(
            "var el = document.querySelector('#tmGoStart, select[name*=\"hour\"], select[id*=\"hour\"]');" +
            "if(el){ el.value='" + HOUR + "'; el.dispatchEvent(new Event('change', {bubbles:true})); }"
        );
        Thread.sleep(300);

        System.out.println("검색 조건 입력 완료: " + DEPARTURE + " → " + ARRIVAL + " (" + dateValue + " " + HOUR + "시)");
    }

    /** 자동완성 팝업 첫 번째 항목 클릭 */
    private void selectFirstAutoComplete() {
        try {
            Thread.sleep(800);
            WebElement firstItem = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector(".autocomplete li:first-child, .station-list li:first-child, ul[role='listbox'] li:first-child")));
            firstItem.click();
        } catch (Exception ignored) {
            // 자동완성이 없으면 그냥 진행
        }
    }

    /** 조회 버튼 클릭 */
    private void clickSearchButton() throws InterruptedException {
        // TODO: F12에서 조회 버튼의 id/class 확인
        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#btnSearch, .btn_search, button[type='submit'], .search-btn")));
        searchBtn.click();
        Thread.sleep(3000);
        System.out.println("열차 조회 완료");
    }

    /** 취소표 모니터링 루프 */
    private void monitorAndReserve() throws InterruptedException {
        while (true) {
            try {
                Thread.sleep(RETRY_INTERVAL_MS);

                // 조회 결과 새로고침 (조회 버튼 재클릭 또는 navigate refresh)
                clickSearchButton();

                // 결과 로드 대기
                // TODO: F12에서 열차 목록 컨테이너의 셀렉터 확인
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".tbl_train_list, .train-list, #tableResult, .result-list")));

                // "예약하기" 버튼 탐색 (매진이 아닌 열차)
                List<WebElement> reserveBtns = driver.findElements(
                        By.xpath("//button[contains(text(),'예약하기')] | //a[contains(text(),'예약하기')] | " +
                                 "//button[contains(@class,'btn_reservation')] | //a[contains(@class,'btn_reservation')]"));

                if (!reserveBtns.isEmpty()) {
                    System.out.println("취소표 발견! 예약 시도...");
                    reserveBtns.get(0).click();
                    Thread.sleep(2000);
                    handleAlertIfPresent();
                    confirmReservation();
                    break;
                }

                System.out.println("[" + java.time.LocalTime.now() + "] 매진. 재시도 중...");

            } catch (Exception e) {
                System.out.println("오류 발생, 재시도: " + e.getMessage());
            }
        }
    }

    /** 브라우저 alert 처리 */
    private void handleAlertIfPresent() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }

    /** 예약 확인/결제 페이지 처리 */
    private void confirmReservation() throws InterruptedException {
        Thread.sleep(2000);
        try {
            // TODO: F12에서 예약 확인 버튼 셀렉터 확인
            WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'결제하기')] | //button[contains(text(),'예약확인')] | " +
                             "//a[contains(text(),'결제하기')] | //button[contains(text(),'확인')]")));
            confirmBtn.click();
            System.out.println("예약 완료! 결제 페이지로 이동했습니다. 5분 안에 결제를 완료하세요.");
        } catch (Exception e) {
            System.out.println("예약 확인 버튼을 찾지 못했습니다. 수동으로 처리하세요: " + e.getMessage());
        }

        // 결제를 위해 브라우저 열어둠 (5분)
        Thread.sleep(300_000);
        driver.quit();
    }
}
