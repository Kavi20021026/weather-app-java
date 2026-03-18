# 🌦️ Weather App (Java Full-Stack)

A simple full-stack weather application built using **pure Java (HttpServer)** and a frontend with HTML, CSS, and JavaScript.

## 🚀 Features

* Search the weather by city
* Real-time weather data using Open-Meteo API
* Lightweight Java backend (no frameworks)
* Clean frontend UI

## 🛠️ Technologies Used

* Java (HttpServer)
* HTML, CSS, JavaScript
* REST API (Open-Meteo)

## 📂 Project Structure

```
src/        # Java backend
web/        # Frontend files
```

## ▶️ How to Run

### 1. Compile

```
javac src/*.java -d out
```

### 2. Run

```
java -cp out App
```

### 3. Open in browser

```
http://localhost:8080
```

## 💡 Future Improvements

* Add weather icons
* 5-day forecast
* Better UI/UX
* Use JSON library (Gson)

## 📸 Screenshots

<img width="1366" height="768" alt="webapp1" src="https://github.com/user-attachments/assets/b6e28de4-6841-4229-b15d-82a52e30133e" />
<img width="1366" height="768" alt="webapp2" src="https://github.com/user-attachments/assets/c15cb0b6-75fc-4733-9caa-8f9139fdd069" />


---
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


## 👨‍💻 Author
Developed by **Kavindu Dissanayaka Madushan**



© 2026 Kavindu Madushan. All rights reserved.
