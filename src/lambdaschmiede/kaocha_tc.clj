(ns lambdaschmiede.kaocha-tc
  "A kaocha plugin to control the lifecycle of Testcontainers using clj-test-containers"
  (:require [kaocha.plugin :as p]
            [clj-test-containers.core :as tc]))

(def ^:dynamic containers-for-scope {})

(defn get-container
  "Retrieves the container configuration with a running container for the given id.
  This function is scoped. If called in a clojure test, it will only return containers
  which match this function, both annotated with `:each` or `:all`"
  [id]
  (get containers-for-scope id))

(defn- containers-for
  "Convenience function to access containers in the config"
  [type config]
  (filter (fn [{:keys [for]}] (= type (:type for))) config))

(defn- start-containers
  "Starts all containers found in the configuration according to their type.
  The type can either be `:each` or `:all`."
  [type kaocha-testcontainers]
  (into {}
        (for [{:keys [id config]} (containers-for type kaocha-testcontainers)]
          [id (tc/start! (tc/create config))])))
(p/defplugin lambdaschmiede.kaocha-tc/plugin

             ;; Wraps the run function, sets up containers before and tears them down afterwards again
             (wrap-run [run config]
                       (let [kaocha-testcontainers (:lambdaschmiede.kaocha-tc/config config)]
                         (fn [test test-plan]
                           (let [containers (case (:kaocha.testable/type test)
                                              :kaocha.type/var (start-containers :each kaocha-testcontainers)
                                              :kaocha.type/ns (start-containers :ns kaocha-testcontainers)
                                              :kaocha.type/clojure.test (start-containers :all kaocha-testcontainers)
                                              {})]
                             (binding [containers-for-scope (merge containers-for-scope containers)]
                               (let [result (run test test-plan)]
                                 (doall (map (fn [[_ container]]
                                               (tc/stop! container)) containers))
                                 result)))))))


(comment
  (require '[kaocha.repl :as k])
  (k/run :unit))