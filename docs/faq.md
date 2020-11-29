
# Frequently Asked Questions

Answers to common questions.

## License: Can I use Corda Testacles with my project?

Yes, Corda Testacles can be used as a library/dependency with no side-effect to your project.
Corda Testacles is distributed under the GNU __Lesser__ General Public License or LGPL, 
the same license adopted by Corda dependencies like Hibernate. 

## OutOfMemoryError during tests

There's a number of reasons for this. Usually you can work around 
the issue by using `jvmArgs` to increase the amount of memory Gradle 
assigns to the tests:

```groovy
test {
    maxParallelForks = 1
    jvmArgs "-Xmx2048m", "-XX:MaxPermSize=2048m"
    useJUnitPlatform {
        includeEngines 'junit-jupiter'
        // ...
    }
}
```