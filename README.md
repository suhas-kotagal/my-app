# Kong Instrumentation Tests

A set of instrumentation test for Kong device. The goal here is to create tests that emulate or represent use cases (Like Zoom calls).


## Best Practices

###  Do

+ Encapsulate setup and tedium in [JUnit rules](https://junit.org/junit4/javadoc/4.12/org/junit/rules/TestRule.html). For example, if every single camera requires certian setup boilerplate code, this setup should be represented with a JUnit test rule. Test rules are intented to introduce modularity in test cases. Tests should represented as succinctly as possible.

+ Use androidx and review [this](https://developer.android.com/training/testing)

+ Annotate tests. (See androidx.test.filters for lists of annotations)

+ Keep in mind this will run in user builds, which means pay attention to things that have selinux ramifications.

+ Comment the tests, tests should be clear to read and understand

+ Document test cases, keep them succinct and focused

+ Make use of logging to help debug issues.

+ Use parameterization for many similiar tests with slightly different parameters.

+ Keep the test app independant of the instrumentation tests. The test app should be able to be run as a standalone app for testing

### Don't

+ No sleeps in tests

+ Avoid mocking logitech objects. We want to test unit modules together. (Mocking listeners callbacks would be an exception to this, it is often easier to verify callbacks on mocked listeners then listener implementations)

+ Typically don't catch exceptions and call Assert.fail() just add the exception to the test method signature. 



# Tests

## 1) Test Main Camera PTZ while rendering and recording using Blueshell API
### Test Case
1. Move camera to known "center position"
2. Assert camera positions
3. start camera and render camera output on surface view
4. move to min camera PTZ ranges
5. Assert camera positions
4. move to max camera PTZ ranges
5. Assert camera positions

Repeat test case with various encoding/decoding formats, FPS etc



# TODO:

## 1) Programatically turn off animations
Set up your test environment
To avoid flakiness, we highly recommend that you turn off system animations on the virtual or physical devices used for testing. On your device, under Settings > Developer options, disable the following 3 settings:

Window animation scale
Transition animation scale
Animator duration scale