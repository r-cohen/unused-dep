# unused-dep
Detects unused dependencies in Android gradle projects

## How does it work?
This tool removes dependencies one by one in an Android gradle project and attempts to run a specified gradle task
in order to verify if the dependency is used or not. If the gradle task succeeds, the tool will mark the dependency as potentially unused. The tool will complete by outputting a list of all potentially unused dependencies.

## Usage
Copy the `unused-dep.jar` file to the root directory where the gradle wrapper (`gradlew` file) is located, and run the following command:
```
java -jar unused-dep.jar -gradletask :app:assembleDebug
```
Replace `:app:assembleDebug` by any gradle task used in your Android project.
