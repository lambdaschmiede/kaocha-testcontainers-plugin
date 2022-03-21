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
  (filter (fn [{:keys [for]}]
            (= type (:type for))) config))

(defn- a-filter-matches [filters value]
  (seq
    (->> filters
         (map #(re-find (re-pattern %) value))
         (filter some?))))

(defmulti start-containers (fn [testable _] (:kaocha.testable/type testable)))

(defmethod start-containers :kaocha.type/var [testable configuration]
  (into {}
        (->> configuration
             (filter #(= :each (get-in % [:for :type])))
             (filter #(a-filter-matches (get-in % [:for :filter]) (:kaocha.testable/desc testable)))
             (map (fn [{:keys [id config]}] [id (tc/start! (tc/create config))])))))

(defmethod start-containers :kaocha.type/ns [testable configuration]
  (into {}
        (->> configuration
             (filter #(= :ns (get-in % [:for :type])))
             (filter #(a-filter-matches (get-in % [:for :filter])  (:kaocha.testable/desc testable)))
             (map (fn [{:keys [id config]}] [id (tc/start! (tc/create config))])))))

(defmethod start-containers :kaocha.type/clojure.test [testable configuration]
  (into {}
        (->> configuration
             (filter #(= :all (get-in % [:for :type])))
             (filter #(some #{(:kaocha.testable/id testable)} (get-in % [:for :tests])))
             (map (fn [{:keys [id config]}] [id (tc/start! (tc/create config))])))))

(p/defplugin lambdaschmiede.kaocha-tc/plugin

             ;; Wraps the run function, sets up containers before and tears them down afterwards again
             (wrap-run [run config]
                       (fn [test test-plan]
                         (let [containers (start-containers test (:lambdaschmiede.kaocha-tc/config config))]
                           (binding [containers-for-scope (merge containers-for-scope containers)]
                             (let [result (run test test-plan)]
                               (doall (map (fn [[_ container]]
                                             (tc/stop! container)) containers))
                               result))))))


(comment
  (require '[kaocha.repl :as k])
  (k/run :unit)
  (k/run :no-tc)
  (k/run-all))