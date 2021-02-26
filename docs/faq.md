
# Frequently Asked Questions

Answers to common questions.

## I find the name offensive ,can you please change it?

Congratulations, your kind is what inspired the name in the first place. 
Have no fear! The Queen's English is here for the rescue. 
As explained in a (now [deleted](https://groups.io/g/corda-dev/message/1475)) 
corda-dev thread, test-a-cles and test-i-cles 
(i.e. testes or gonads) are simply different words. 
 
The project is Open Source, go head and fork under a different name 
if you need to. Finding something else to offend you might be more 
appropriate though.

## License: Can I use Corda Testacles with my project?

Absolutely. Corda Testacles can be used as a library/dependency 
with no side-effect to your project. More specifically, Corda 
Testacles is distributed under the GNU __Lesser__ General Public 
License (LGPL in short), the same license adopted by Corda 
dependencies like Hibernate. 

## OutOfMemoryError during tests

There's a number of reasons for this. Usually you can work around 
the issue by using `jvmArgs` to increase the amount of memory Gradle 
assigns to the tests:

```groovy
test {
    maxParallelForks = 1
    maxHeapSize = "3g"
    jvmArgs = listOf("-Xmx6144m")
    useJUnitPlatform {
        includeEngines 'junit-jupiter'
        // ...
    }
}
```