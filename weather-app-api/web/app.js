const form = document.getElementById("weather-form");
const cityInput = document.getElementById("city");
const result = document.getElementById("result");
const statusText = document.getElementById("status");

function formatNumber(value, suffix = "") {
    return `${Number(value).toFixed(1)}${suffix}`;
}

function showWeather(data) {
    result.classList.remove("hidden");
    result.innerHTML = `
        <h2>${data.city}, ${data.country}</h2>
        <p class="condition">${data.condition} • Updated ${data.updatedAt}</p>
        <div class="metrics">
            <div class="metric">
                <span>Temperature</span>
                <strong>${formatNumber(data.temperatureC, "°C")}</strong>
            </div>
            <div class="metric">
                <span>Feels Like</span>
                <strong>${formatNumber(data.feelsLikeC, "°C")}</strong>
            </div>
            <div class="metric">
                <span>Humidity</span>
                <strong>${formatNumber(data.humidity, "%")}</strong>
            </div>
            <div class="metric">
                <span>Wind Speed</span>
                <strong>${formatNumber(data.windSpeedKmh, " km/h")}</strong>
            </div>
            <div class="metric">
                <span>Precipitation</span>
                <strong>${formatNumber(data.precipitationMm, " mm")}</strong>
            </div>
            <div class="metric">
                <span>Weather Code</span>
                <strong>${data.weatherCode}</strong>
            </div>
        </div>
    `;
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const city = cityInput.value.trim();
    if (!city) {
        statusText.textContent = "Please enter a city name.";
        return;
    }

    statusText.textContent = "Loading weather data...";
    result.classList.add("hidden");

    try {
        const response = await fetch(`/api/weather?city=${encodeURIComponent(city)}`);
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Something went wrong.");
        }

        showWeather(data);
        statusText.textContent = "";
    } catch (error) {
        statusText.textContent = error.message;
    }
});
