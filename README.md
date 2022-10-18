# Round Up App

An app that takes all of a users transactions in a given week and rounds them up to the nearest pound, providing the option to add this amount to a savings
goal.

Built using Android Studio Dolphin | 2021.3.1

## Setup

### Add your sandbox customers access token

1. Create a Starling API developer account ([link](http://developer.starlingbank.com/signup))
2. Create an API application (the details you provide do not matter) ([link](https://developer.starlingbank.com/application/list))
3. Create a sandbox customer ([link](https://developer.starlingbank.com/sandbox/select))
4. Scroll to "Simulator" and click "Auto-simulate" to generate transactions for your customer
5. Add your customers "Access Token" to `NetworkModule#provideAccessToken` in the `:network` module

### Ensure that you build the app using Java 11

`Android Studio -> Settings/Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle -> Set "Gradle JDK" to '11'`

## Module structure

* `:app` - Code that launches the app & integration tests
* `:buildSrc`- Gradle configuration used across all modules (e.g. dependencies, versions, plugins, etc.)
* `:data` - Repositories that fetch data (using data sources) & map the responses to "clean" models
* `:network` - Data sources that fetch data over the network
* `:features` - Parent of submodules that represent application features
    * `:features:roundup` - Parent module of the round up feature
        * `:features:roundup:ui` - The UI layer of the round up feature (Composable's, ViewModel's)
        * `:features:roundup:domain` - The domain layer of the round up feature (UseCase's)
* `:core` - Parent of submodules that contain logic that is used throughout the application
    * `:core:ui` - UI constants/extensions
    * `:core:models` - Model classes
    * `:core:factories` - Factories used to construct & provide platform classes (eases testing)
    * `:core:test` - Unit test utility classes, also exposes all required unit test dependencies

## Libraries used

Jetpack Compose, Hilt, Kotlin Coroutines, Kotlin Flow, Retrofit, OkHttp, Moshi, JUnit5, MockK, Turbine
