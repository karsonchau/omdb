# OMDb

OMDb is an Android application that allows users to search for movies using the OMDb API. Users can search by title, year, and type to retrieve relevant movie results.

## Features
- Search for movies by title.
- Filter results by year and type (movie, series, or episode).
- Display search results in a user-friendly interface.

## Getting Started
### Prerequisites
- Android Studio installed on your system.
- A valid OMDb API key.

### Getting an OMDb API Key
To use this application, you need an API key from OMDb. Follow these steps:
1. Visit [OMDb API Key Request](https://www.omdbapi.com/apikey.aspx)
2. Sign up for an API key.
3. Use the API key when making requests.

### Installation
1. Clone the repository:
   ```sh
   git clone https://github.com/karsonchau/OMDb.git
   ```
2. Open the project in Android Studio.
3. Add your OMDb API key in the `local.properties` file in the following format:
   ```
   API_KEY=[YOUR_API_KEY]
   ```
4. Build and run the project on an emulator or physical device.

## Usage
1. Enter a movie title in the search bar.
2. Optionally, specify the year and type (movie, series, or episode).
3. Press the search button to retrieve results.
4. View movie details in the search results.

## Technologies Used
- Kotlin
- Jetpack Compose
- Retrofit
- Dagger Hilt
- Coil
- Mockito
- MVVM architecture

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments
- [OMDb API](https://www.omdbapi.com/) for providing movie data.


