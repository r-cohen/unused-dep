# unused-dep
Detects unused dependencies in Android gradle projects

## How does it work?
This tool removes dependencies one by one in an Android gradle project and attempts to run a specified gradle task
in order to verify if the dependency is used or not. If the gradle task succeeds, the tool will mark the dependency as potentially unused. The tool will complete by printing a list of all potentially unused dependencies.

## Usage
Copy the `unused-dep.jar` file to the root directory where the gradle wrapper (`gradlew` file) is located, and run the following command:
```
java -jar unused-dep.jar -gradletask :app:assembleDebug
```
Replace `:app:assembleDebug` by any gradle task used in your Android project.

### Filter out dependencies
It is possible to skip dependencies using the `-skip` flag and provide a comma seperated list of depencies to exclude for the tests. Example:
```
java -jar unused-dep.jar -gradletask :app:assembleDebug -skip com.android.support.test:runner:1.0.2,com.android.support:appcompat-v7:28.0.0
```

[<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" height="60" />](https://www.buymeacoffee.com/raphael.cohen)
