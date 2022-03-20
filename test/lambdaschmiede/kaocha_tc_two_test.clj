(ns lambdaschmiede.kaocha-tc-two-test
  (:require [clojure.test :refer :all]
            [lambdaschmiede.kaocha-tc :refer [get-container]])
  (:import [org.testcontainers.containers GenericContainer]))

(deftest for-each-test-2

  (testing "The 'for each' container exists"
    (let [pg-container (get-container :postgres-2)]
      (is (some? pg-container))
      (is (some? (.getHost ^GenericContainer (:container pg-container))))
      (is (some? (.getMappedPort (:container pg-container) 5432))))))

(deftest other-for-each-test-2

  (testing "The 'for each' container exists"
    (let [pg-container (get-container :postgres-2)]
      (is (some? pg-container))
      (is (some? (.getHost ^GenericContainer (:container pg-container))))
      (is (some? (.getMappedPort (:container pg-container) 5432))))))

(deftest another-for-each-test-2

  (testing "The 'for each' container exists"
    (let [pg-container (get-container :postgres-2)]
      (is (some? pg-container))
      (is (some? (.getHost ^GenericContainer (:container pg-container))))
      (is (some? (.getMappedPort (:container pg-container) 5432))))))

(deftest for-all-test-2

  (testing "The 'for all' container exists"
    (let [pg-container (get-container :postgres-1)]
      (is (some? pg-container))
      (is (some? (:host pg-container)))
      (is (some? (.getMappedPort (:container pg-container) 5432)))))

  (testing "The 'for each' container also exists"
    (let [pg-container (get-container :postgres-2)]
      (is (some? pg-container))
      (is (some? (.getHost ^GenericContainer (:container pg-container))))
      (is (some? (.getMappedPort (:container pg-container) 5432))))))