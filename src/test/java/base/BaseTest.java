// package base;

// import com.microsoft.playwright.*;
// import org.testng.annotations.AfterClass;
// import org.testng.annotations.BeforeClass;
// import org.testng.annotations.BeforeMethod;

// import java.nio.file.Paths;
// import java.util.List;

// public class BaseTest {

//     protected Playwright playwright;
//     protected Browser browser;
//     protected BrowserContext context;
//     protected Page page;

//     protected final String BASE_URL = "https://shopping.santabrowser.com/?uuid=2da68a9dd8c5a73";
//    // "https://shopping.santabrowser.com/?uuid=8da91acc7b09930";

//     @BeforeClass
//     public void setUp() {

//         playwright = Playwright.create();

//         browser = playwright.chromium().launch(
//                 new BrowserType.LaunchOptions()
//                         .setExecutablePath(Paths.get("C:\\Users\\DELL\\AppData\\Local\\Santa\\Application\\santa.exe"))
//                         .setHeadless(false)
//                         .setSlowMo(1000)
//                         .setArgs(List.of("--start-maximized"))
//         );

//         context = browser.newContext(
//                 new Browser.NewContextOptions()
//                         .setViewportSize(null)
//         );

//         page = context.newPage();

//         System.out.println("Browser launched");
//     }

//     @BeforeMethod
//     public void openCashback() {

//         System.out.println("Opening Cashback Home Page");

//         page.navigate(BASE_URL);
//     }

//     @AfterClass
//     public void tearDown() {

//         if (context != null)
//             context.close();

//         if (browser != null)
//             browser.close();

//         if (playwright != null)
//             playwright.close();

//         System.out.println("Browser Closed");
//     }

// public Page getPage() {
//     return this.page;
// }

// }


package base;

import com.microsoft.playwright.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.util.List;

public class BaseTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    protected final String BASE_URL = "https://shopping.santabrowser.com/?uuid=2da68a9dd8c5a73";
    // "https://shopping.santabrowser.com/?uuid=8da91acc7b09930";

    // Headless in CI (GitHub Actions sets CI=true automatically), headed+slow locally so you can watch it.
    // Override explicitly with -Dheadless=true or -Dheadless=false if you ever need to force it.
    private static final boolean IS_CI = "true".equalsIgnoreCase(System.getenv("CI"));
    private static final boolean HEADLESS = Boolean.parseBoolean(
            System.getProperty("headless", String.valueOf(IS_CI))
    );

    @BeforeClass
    public void setUp() {

        playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(HEADLESS);

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();

        if (HEADLESS) {
            // No real display in CI, so give the context a fixed viewport instead of null.
            contextOptions.setViewportSize(1366, 768);
        } else {
            launchOptions.setSlowMo(1000);
            launchOptions.setArgs(List.of("--start-maximized"));
            contextOptions.setViewportSize(null);
        }

        browser = playwright.chromium().launch(launchOptions);
        context = browser.newContext(contextOptions);
        page = context.newPage();

        System.out.println("Browser launched (headless=" + HEADLESS + ")");
    }

    @BeforeMethod
    public void openCashback() {
        System.out.println("Opening Cashback Home Page");
        page.navigate(BASE_URL);
    }

    @AfterClass
    public void tearDown() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        System.out.println("Browser Closed");
    }

    public Page getPage() {
        return this.page;
    }
}