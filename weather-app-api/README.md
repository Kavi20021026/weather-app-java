# Weather App API

A small Java weather application with:

- A custom API endpoint: `/api/weather?city=Colombo`
- A browser UI served from the same app
- Live weather data from the Open-Meteo API

## Project Structure

- `src/` contains the Java server and weather logic
- `web/` contains the HTML, CSS, and JavaScript frontend

## Run the Project

Open PowerShell in:

`C:\Users\Kavindu Dissanayaka\Desktop\Projects\weather-app-api`

Compile:

```powershell
javac -d out src\*.java
```

Run:

```powershell
java -cp out App
```

Then open:

[http://localhost:8080](http://localhost:8080)

## API Example

```text
GET /api/weather?city=Colombo
```

Example response:

```json
{
  "city": "Colombo",
  "country": "Sri Lanka",
  "latitude": 6.93,
  "longitude": 79.85,
  "updatedAt": "2026-03-18T10:30",
  "temperatureC": 30.2,
  "feelsLikeC": 36.0,
  "humidity": 74.0,
  "windSpeedKmh": 12.6,
  "precipitationMm": 0.0,
  "weatherCode": 2,
  "condition": "Partly cloudy"
}
```

## Notes

- This project uses only core Java, so no Maven or Gradle is required.
- Internet access is needed at runtime because the app fetches live weather data.
