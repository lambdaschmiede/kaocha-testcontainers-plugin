# Kaocha Testcontainers Plugin

## What does it do?

This is a plugin for the [kaocha](https://github.com/lambdaisland/kaocha) testrunner. It manages the lifecylce
of [Testcontainers](https://github.com/testcontainers/testcontainers-java).

This is a very early work in progress and the configuration or the API might still break.

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

## How to use it?

This plugin can be registered as a kaocha plugin. The container definition is part of the configuration, too. The `:id`
specifies the identifier which can be used to later access containers from the tests. `:for` determines if the container
should be started for all tests, or should be created for each single test and then discarded afterwards. The `:config`
contains a container config according to
the [`create` function of clj-test-containers](https://github.com/javahippie/clj-test-containers#create):

```clojure
#kaocha/v1
    {:tests                           [{:id          :unit
                                        :test-paths  ["test"]
                                        :ns-patterns [".*test$"]
                                        :fail-fast?  true}

                                       {:id          :no-tc
                                        :test-paths  ["test"]
                                        :ns-patterns [".*no-tc$"]}]

     :kaocha/plugins                  [:lambdaschmiede.kaocha-tc/plugin]
     :lambdaschmiede.kaocha-tc/config [
                                       {:id     :postgres-1
                                        :for    {:type   :all
                                                 :filter [:unit]}
                                        :config {:image-name    "postgres:12.1"
                                                 :exposed-ports [5432]
                                                 :env-vars      {"POSTGRES_PASSWORD" "verysecret"}}}

                                       {:id     :postgres-2
                                        :for    {:type   :each
                                                 :filter ["-test$"]}
                                        :config {:image-name    "postgres:12.1"
                                                 :exposed-ports [5432]
                                                 :env-vars      {"POSTGRES_PASSWORD" "verysecret"}}}

                                       {:id     :postgres-3
                                        :for    {:type   :ns
                                                 :filter ["test$"]}
                                        :config {:image-name    "postgres:12.1"
                                                 :exposed-ports [5432]
                                                 :env-vars      {"POSTGRES_PASSWORD" "verysecret"}}}]}
```

Note, that we can assign different scopes to the containers via `:for`: `:postgres-1` will start before a whole test run
and be discarded afterwards, marked by the `:all` type. The `:filter` vector accepts a list of test-ids for which this
should be run. In this case, the container would be started before the `:unit` tests, but not before the `:no-tc`
tests. `postgres` will be started for every single test which name ends in `-test`, `:postgres-3` will be started for 
every test namespace which name ends on `test`. 

If we now want to access the container and its configuration from within the tests, we can do so with the
function `lambdaschmiede.kaocha-tc/get-container`. This function is scoped, so it will only return containers which are
valid for the active test:

```clojure
(require [clojure.test :refer :all])
(require [lambdaschmiede.kaocha-tc/get-container :refer [get-container]])
(deftest for-each-test

         (testing "The 'for each' container exists"
                  (let [pg-container (get-container :postgres-2)]
                    (is (some? pg-container))
                    (is (some? (.getHost ^GenericContainer (:container pg-container))))
                    (is (some? (.getMappedPort (:container pg-container) 5432))))))
```

## License

Distributed under the Eclipse Public License either version 2.0 or (at your option) any later version.