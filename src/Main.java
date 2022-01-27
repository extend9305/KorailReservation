import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;


public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        while (true){
            try {
                main.crawl();
            }catch (Exception e){
                main.crawl();
            }
        }






    }


    //WebDriver
    private WebDriver driver;

    //Properties
    public static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    public static final String WEB_DRIVER_PATH = "C:/workspace/KorailReservation/etc/chromedriver_win32/chromedriver.exe";

    //크롤링 할 URL
    private String base_url;

    public Main() {
        super();

        //System Property SetUp
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

        //Driver SetUp
        driver = new ChromeDriver();
        base_url = "https://www.letskorail.com/korail/com/login.do";


    }

    public void crawl() {

        try {
            //get page (= 브라우저에서 url을 주소창에 넣은 후 request 한 것과 같다)
            driver.get(base_url);
            //System.out.println(driver.getPageSource());

            Thread.sleep(300);
            WebElement id =(WebElement) driver.findElement(By.id("txtMember"));
            id.sendKeys("");
            Thread.sleep(300);
            WebElement pwd =(WebElement) driver.findElement(By.id("txtPwd"));
            pwd.sendKeys("");
            Thread.sleep(300);

            WebElement btn_login = (WebElement) driver.findElement(By.className("btn_login"));
            btn_login.click();
            Thread.sleep(10000);

            JavascriptExecutor exe = (JavascriptExecutor)driver;
            exe.executeScript("m_menuPrd1_1_1_link()");


            Thread.sleep(2000);
            WebElement s_month =(WebElement) driver.findElement(By.id("s_month"));
            s_month.sendKeys("2");
            Thread.sleep(300);


            WebElement s_day =(WebElement) driver.findElement(By.id("s_day"));
            s_day.sendKeys("5");
            Thread.sleep(300);

            WebElement btn_inq = (WebElement) driver.findElement(By.className("btn_inq"));
            btn_inq.click();
            Thread.sleep(300);


            WebElement start =(WebElement) driver.findElement(By.id("start"));
            start.clear();
            start.sendKeys("영등포");
            Thread.sleep(300);

            WebElement get =(WebElement) driver.findElement(By.id("get"));
            get.clear();
            get.sendKeys("대전");
            Thread.sleep(300);

            WebElement s_hour =(WebElement) driver.findElement(By.id("s_hour"));
            s_hour.sendKeys("18");
            Thread.sleep(300);

            WebElement btn_inq2 = (WebElement) driver.findElement(By.className("btn_inq"));
            btn_inq2.click();
            Thread.sleep(300);

            JavascriptExecutor exe2 = (JavascriptExecutor)driver;
            exe2.executeScript("inqSchedule()");

            String str = "";
            int first = 0;
            int last = 0;
            while(true){
                Thread.sleep(3000);
                exe2.executeScript("inqSchedule()");

                Thread.sleep(500);
                str = driver.getPageSource();

                first = str.indexOf("<table id=\"tableResult\"");
                last = str.indexOf("</tbody></table>");
                System.out.println("first = " + first);
                System.out.println("last = " + last);
                if(first == -1) continue;
                str = str.substring(first,last);

                System.out.println(str);

                if(str.indexOf("예약하기")>0){
                    break;
                }
            }

            String xpath = "//*[contains(@src,'icon_apm_rd.gif')]";
            WebElement el = (WebElement) driver.findElement(By.xpath(xpath));
            el.click();

            String thtml ="";
            if(ExpectedConditions.alertIsPresent().apply(driver)==null){
                //알림창이 없으면 아무것도 하지 말것
            }else{
                //알림창이 존재하면 알림창 확인을 누를것
                driver.switchTo().alert().accept();
            }



            Thread.sleep(300000);



        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            driver.close();
        }

    }

}

