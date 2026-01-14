Feature: Royal Brothers Bike Booking

  Scenario Outline: Validate bike availability for selected city, location and model
    Given user opens Royal Brothers website
    When user selects city "<city>"
    And user selects booking time from "<start>" to "<end>"
    And user clicks on search
    Then selected date and filters should be visible
    Then all bikes listed should belong to "<location>"
    Then all shown bike cards should match bike model "<bikeModel>" and show availability

    Examples:
      | city      | start              | end                | location    | bikeModel      |
      | Bangalore | 2026-01-15 10:00   | 2026-01-16 10:00   | Indiranagar | Honda Activa   |