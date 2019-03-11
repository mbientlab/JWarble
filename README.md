# JWarble
JWarble provides a Java 8 API around the [Warble C library](https://github.com/mbientlab/Warble).

# Install
Update the repositories closure to include the MbientLab Ivy repo in the project's ``build.gradle`` file:

```groovy
repositories {
    ivy {
        url "https://mbientlab.com/releases/ivyrep"
    }
}
```

Then, add an ``implementation`` element to the repository closure with the 'com.mbientlab:warble' id:

```groovy
dependencies {
    implementation 'com.mbientlab:warble:1.0.0'
}
```

# Usage
See the [unit tests](https://github.com/mbientlab/JWarble/blob/master/src/test/java/com/mbientlab/warble/Example.java) unit test for examples 
on how to perform a BLE scan, connect to a remote device, and read device information.