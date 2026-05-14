# Random Soccer Predictor

**Random Soccer Predictor** is an Android application designed to predict soccer match outcomes. It utilizes statistical methods (Poisson distribution) combined with current world ranking data to calculate realistic match results and probabilities.

## 🚀 Features

-   **Result Prediction**: Calculates goals for both teams based on their relative strength.
-   **Probability Analysis**: Displays winning chances for home and away teams, as well as draw probabilities.
-   **World Rankings**: Built-in list of national teams with their current scores.
-   **Knockout Mode**: Option for tournament matches where a draw is excluded (simulating extra time/penalties).
-   **Customizable Parameters**: Users can fine-tune the mathematical models in the settings.
-   **Bilingual**: Full support for English and German.

## 🛠 How it Works & Mathematics

Predictions are not based on pure randomness but on a weighted probability distribution:

1.  **Poisson Distribution**: The foundation for calculating goal probabilities.
2.  **Ranking Influence**: The difference in world ranking points influences the expected value (Lambda) for goals. A stronger team has a higher probability of scoring.
3.  **Goal Decay**: An adjustable factor that realistically dampens the likelihood of extremely high scores (e.g., 8-0).

## ⚙️ Settings

The following parameters can be adjusted in the settings:

-   **Max Goals**: Limits the number of goals per team (Default: 6).
-   **Ranking Influence**: Determines how much the world ranking difference affects the outcome.
-   **Goal Decay**: Controls how quickly the probability of additional goals decreases.

## 📱 Tech Stack

-   **Language**: Kotlin
-   **Architecture**: MVVM (Model-View-ViewModel)
-   **UI**: XML / Material Design 3
-   **Navigation**: Jetpack Navigation Component
-   **Data Storage**: CSV-based world rankings (`data/ranks.csv`)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
