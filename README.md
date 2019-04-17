# Forecast
A practice weather app made with RxJava and Retrofit

Proposition:

The task is to simply build a forecast mobile application. The requirements are the following:

- The user must be able to do a search by the city name. 
- Each time the user searches the forecast for a city the result should be shown indicating:
- Temperature
- Pressure
- Humidity
- Max temperature
- Min temperature
- A Map showing the location of the city (using long and lat)
- You need to maintain a list with the last 5 searched cities (it should be persisted in local storage). Any item of the list can be deleted.
- Every time a city from the above list is clicked, the forecast information should be shown as in step (2).
- Add at least one additional feature that you think would be useful for the user.
  In this case I added two extras: 
  1- Being able to refresh the selected location. 
  2- Loading the current location weather when no other location is specified. 
- The forecast prediction must be consumed using this api: https://openweathermap.org/current
