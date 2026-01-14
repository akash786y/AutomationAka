package steps;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.nio.file.Paths;
import java.util.List;

public class RoyalBrothersSteps {

    Playwright playwright;
    Browser browser;
    Page page;

    @Given("user opens Royal Brothers website")
    public void openWebsite() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
        );
        page = browser.newPage();
        page.navigate("https://www.royalbrothers.com/");
        Assert.assertTrue(page.title().contains("Royal Brothers"));
    }


    @When("user selects city {string}")
    public void selectCity(String city) {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(2000);

        String currentUrl = page.url();
        String expectedCityUrl = city.toLowerCase();

        if (currentUrl.contains(expectedCityUrl)) {
            System.out.println("Already on " + city + " page, skipping city selection");
            return;
        }

        page.navigate(String.format("https://www.royalbrothers.com/%s/bike-rentals", expectedCityUrl));
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @And("user selects booking time from {string} to {string}")
    public void selectBookingTime(String start, String end) {

        page.locator("#pickup-date-desk").click();
        page.locator("#pickup-time-desk").first().click();
        page.locator("#dropoff-date-desk").click();
        page.locator("#dropoff-time-desk").first().click();
    }


    @And("user clicks on search")
    public void clickSearch() {
        page.click("button:has-text('Apply filter')");
    }

    @Then("selected date and filters should be visible")
    public void validateFilters() {
        Assert.assertTrue(page.url().contains("/search"));
        String pickupDate = page.locator("#pickup-date-desk").getAttribute("data-selected");
        String dropoffDate = page.locator("#dropoff-date-desk").getAttribute("data-selected");
        String pickupTime = page.locator("#pickup-time-desk").getAttribute("data-selected");
        String dropoffTime = page.locator("#dropoff-time-desk").getAttribute("data-selected");
        Assert.assertNotNull(pickupDate, "Pickup date should be displayed");
        Assert.assertNotNull(dropoffDate, "Dropoff date should be displayed");
        Assert.assertNotNull(pickupTime, "Pickup time should be displayed");
        Assert.assertNotNull(dropoffTime, "Dropoff time should be displayed");
        System.out.println("Pickup: " + pickupDate);
        System.out.println("Dropoff: " + dropoffDate);
    }

    @When("user applies bike model filter {string}")
    public void applyBikeModelFilter(String bikeModel) {
        Locator checkbox = page
                .locator("ul.bike_model_listing li label")
                .filter(new Locator.FilterOptions().setHasText(bikeModel))
                .locator("input[type='checkbox']")
                .first();

        checkbox.waitFor();
        if (!checkbox.isChecked()) {
            checkbox.check();
            System.out.println("Applied bike model filter: " + bikeModel);
        } else {
            System.out.println("Bike model filter already applied: " + bikeModel);
        }
        page.waitForCondition(() -> checkbox.isChecked());
    }


    @Then("all bikes listed should belong to {string}")
    public void validateBikeLocation(String location) {
        Locator locationCheckbox = page
                .locator("ul.location_listing li label")
                .filter(new Locator.FilterOptions().setHasText(location))
                .locator("input[type='checkbox']")
                .first();
        page.waitForCondition(() -> locationCheckbox.isChecked());
        Assert.assertTrue(
                locationCheckbox.isChecked(),
                "Location filter is NOT checked: " + location
        );
        System.out.println("Location filter confirmed: " + location);
        Locator bikes = page.locator("ul.bike_model_listing li.each_list label");
        int count = bikes.count();
        Assert.assertTrue(count > 0, "No bikes found after applying filters");
        System.out.println("Bikes available at " + location + ":");

        for (int i = 0; i < count; i++) {
            String bike = bikes.nth(i).innerText().trim();
            System.out.println(" - " + bike);
        }
    }

    @Then("all shown bike cards should match bike model {string} and show availability")
    public void validateBikeCards(String expectedModel) {
        Locator bikeCards = page.locator("search_page_row.each_card_form").first();

        int cardCount = bikeCards.count();

        for (int i = 0; i < cardCount; i++) {
            Locator card = bikeCards.nth(i);

            String modelName = card.locator("h6.bike_name").innerText().trim();
            String pickupDate = card.locator("label#pickup_date").innerText().trim();
            String dropoffDate = card.locator("label#dropoff_date").innerText().trim();
            String location = card.locator(".location-display").innerText().trim(); // adjust selector if needed

            String availabilityStatus = card.locator("select[name='location'] option[selected]")
                    .getAttribute("data-status");

            Assert.assertTrue(
                    modelName.toLowerCase().contains(expectedModel.toLowerCase()),
                    "Unexpected bike model: " + modelName
            );
            Assert.assertFalse(pickupDate.isEmpty(), "Pickup date missing for " + modelName);
            Assert.assertFalse(dropoffDate.isEmpty(), "Dropoff date missing for " + modelName);
            Assert.assertEquals(availabilityStatus, "available", "Bike not available: " + modelName);

            System.out.println(
                    "Verified -> " + modelName +
                            " | Location: " + location +
                            " | Pickup: " + pickupDate +
                            " | Dropoff: " + dropoffDate
            );
        }
    }
}