package com.braintreepayments.popupbridge.example.test;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.view.View;
import android.webkit.WebView;

import com.braintreepayments.popupbridge.example.MainActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withContentDescription;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PayPalCheckoutPopupBridgeTest {

    private static final long BROWSER_TIMEOUT = 60000;
    private static final String PAYPAL_POPUPBRIDGE_EXAMPLE_URL = "https://braintree.github.io/popup-bridge-example/paypal-checkout.html";
    private static final String SANDBOX_PAYPAL_USERNAME = "sandbox-user@paypal.com";
    private static final String SANDBOX_PAYPAL_PASSWORD = "passw0rd";

    @Before
    public void setup() {
        Intent intent = InstrumentationRegistry.getContext()
                .getPackageManager()
                .getLaunchIntentForPackage("com.braintreepayments.popupbridge.example")
                .putExtra(MainActivity.EXTRA_URL, PAYPAL_POPUPBRIDGE_EXAMPLE_URL);

        onDevice().onHomeScreen().launchApp(intent);
        onDevice(withContentDescription("PayPal PopupBridge Example")).waitForExists(BROWSER_TIMEOUT);
    }

    @Test
    public void opensCheckout_returnsPaymentToken() throws UiObjectNotFoundException {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        onDevice(withContentDescription("The safer, easier way to pay")).perform(click());
        login(uiDevice);
        onDevice(withContentDescription("Pay with")).waitForExists(BROWSER_TIMEOUT);

        UiObject2 webview = uiDevice.findObject(By.clazz(WebView.class));
        webview.scroll(Direction.DOWN, 100);

        onDevice(withContentDescription("Pay Now")).perform(click());

        onDevice(withContentDescription("The safer, easier way to pay")).waitForExists(BROWSER_TIMEOUT);

        List<UiObject2> views = uiDevice.findObjects(By.clazz(View.class));
        UiObject2 logView = views.get(views.size()-1);
        String log = logView.getContentDescription();

        assertTrue(log.contains("\"paymentToken\": \"EC-"));
        assertTrue(log.contains("\"intent\": \"sale"));
        assertTrue(log.contains("returnUrl"));
    }

    private void login(UiDevice uiDevice) {
        try {
            // Force a login, otherwise continue
            onDevice(withContentDescription("Not you?"))
                    .waitForExists(TimeUnit.SECONDS.toMillis(2))
                    .perform(click());
        } catch (RuntimeException ignored) {}

        onDevice(withContentDescription("Log In"))
                .waitForExists(BROWSER_TIMEOUT);

        List<UiObject2> editTexts = uiDevice.findObjects(By.clazz("android.widget.EditText"));
        int lastElement = editTexts.size() - 1;

        UiObject2 loginEditText = editTexts.get(lastElement - 1);
        UiObject2 passwordEditText = editTexts.get(lastElement);

        loginEditText.setText(SANDBOX_PAYPAL_USERNAME);
        passwordEditText.setText(SANDBOX_PAYPAL_PASSWORD);

        onDevice(withContentDescription("Log In")).perform(click());
    }
}
