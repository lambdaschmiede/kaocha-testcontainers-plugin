# Kaocha Testcontainers Plugin

## What does it do?

This is a plugin for the [kaocha](https://github.com/lambdaisland/kaocha) testrunner. It manages the lifecylce
of [Testcontainers](https://github.com/testcontainers/testcontainers-java).

## Who is it for?

Managing the state of Testcontainers in tests manually, e.g.
with [clj-test-containers](https://github.com/javahippie/clj-test-containers), allows us to control in great detail
which containers are created and destroyed at what time. As a downside, the setup code for the containers often
distracts from the domain we want to test and is repetitive. This plugin helps developers define a set of containers
which will be made available for test – either with a global scope for all tests, or with a local scope for each test.

## What is still missing?

This is a work in progress. We are currently working on adding additional filters, so the plugin can control in which
test types the Testcontainers are used and add namespace filters to the containers to allow for a finer association from
containers to tests

## License

Distributed under the Eclipse Public License either version 2.0 or (at your option) any later version.