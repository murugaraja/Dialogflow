package com.vdsi.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

/**
 *  This REST API Controller is to get the weather details from third party API and respond back
 */
@Controller
public class WeatherCheck {

    private RestTemplate restTemplate;
    private HttpEntity<String> entity;
    Logger logger = LoggerFactory.getLogger(WeatherCheck.class);

    /**
     *  Controller to initialize required values
     */
    public WeatherCheck() {
        restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RapidAPI-Host", "community-open-weather-map.p.rapidapi.com");
        headers.add("X-RapidAPI-Key", "fc67f3f91bmsh320e9900032c024p1b8d4ajsn4802882b9f61");
        entity = new HttpEntity<String>("parameters", headers);
    }

    /**
     *  This getWeather API is a POST API and it will be called by google dialogflow
     *  We have to configure this URI in dialogflow like http://x.x.x.x:8080/getWeather
     * @param requestBody request body received from google dialogflow
     * @return return fulfillment text message to the bot
     */
    @RequestMapping(path = "/getWeather", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getWeather(@RequestBody String requestBody) {

        logger.info("Request String :" + requestBody);
        String WeatherRawResponse = getWeatherRawResponse("chennai");
        String kelvin = extractKelvin(WeatherRawResponse);
        int celsius = kelvinToCelsius(kelvin);

        String message = "\"Current weather in chennai is "+celsius+" degree celsius \"";
        logger.info(message);

        String responseMessage = "{ \"fulfillmentText\": "+message +"  }";
        return  ResponseEntity.ok(responseMessage);
    }

    /**
     * Here using "rapidapi" we are getting the weather details of the city
     * @param city The city we want to get the weather details
     * @return Raw response from "rapidapi"
     */
    private String getWeatherRawResponse(String city) {
        final String uri = "https://community-open-weather-map.p.rapidapi.com/weather?callback=test&id=2172797" +
                "&units=\"metric\" or \"imperial\"&mode=json, html&q=" + city + ",in";
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return result.toString();
    }

    /**
     *  Due to time constrain I have read the kelvin values directly instead of using in-build utils like gson, etc
     * @param weatherRawResponse RawResponse got from "rapidapi"
     * @return kelvin value return extracted from "rapidapi" response
     */
    public String extractKelvin(String weatherRawResponse) {
        String splitString[] = weatherRawResponse.split("temp\":");
        int i = splitString[1].lastIndexOf("pressure");
        return splitString[1].substring(0, i - 2);
    }

    /**
     * Convert kelvin to Celsius
     * @param kelvinString kelvin value as string
     * @return celsius value corresponding to the kelvin value
     */
    public int kelvinToCelsius(String kelvinString) {
        Float kelvin = Float.parseFloat(kelvinString);
        Float celsius = kelvin - 273.15F;
        return celsius.intValue();
    }
}