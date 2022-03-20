(ns lambdaschmiede.kaocha-tc
  "A kaocha plugin to control the lifecycle of Testcontainers using clj-test-containers"
  (:require [kaocha.plugin :as p]
            [clj-test-containers.core :as tc]))

(def ^:dynamic for-all-scope {})
(def ^:dynamic for-each-scope {})

(defn get-container
  "Retrieves the container configuration with a running container for the given id.
  This function is scoped. If called in a clojure test, it will only return containers
  which match this function, both annotated with `:each` or `:all`"
  [id]
  (get (merge for-each-scope for-all-scope) id))

(defn- containers-for
  "Convenience function to access containers in the config"
  [type config]
  (filter (fn [{:keys [for]}] (= type for)) config))

(defn- start-containers
  "Starts all containers found in the configuration according to their type.
  The type can either be `:each` or `:all`."
  [type kaocha-testcontainers]
  (into {}
        (for [{:keys [id config]} (containers-for type kaocha-testcontainers)]
          [id (tc/start! (tc/create config))])))

(defn- wrap-with-containers
  "Returns a function that starts all matching `:each` containers before each step and removes them again, afterwards.
  Meant to be registered in `:kaocha.testable/wrap` on the testable."
  [config t]
  #(binding [for-each-scope (start-containers :each config)]
     (let [res (t)]
       (for [[_ container] for-each-scope]
         (tc/stop! container))
       res)))

(p/defplugin lambdaschmiede.kaocha-tc/plugin

             ;; Runs before each individual test
             (pre-test [test {:keys [kaocha-testcontainers]}]
                       (update test :kaocha.testable/wrap conj (partial wrap-with-containers kaocha-testcontainers)))

             ;; Allows "wrapping" the run function
             (wrap-run [run {:keys [kaocha-testcontainers]}]
                       (fn [test test-plan]
                         (binding [for-all-scope (start-containers :all kaocha-testcontainers)]
                           (let [result (run test test-plan)]
                             (tc/perform-cleanup!)
                             result)))))


(comment
  (require '[kaocha.repl :as k])
  (k/run :unit)

  for-all-scope
  )