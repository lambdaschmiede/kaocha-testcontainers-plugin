(ns lambdaschmiede.kaocha-tc-test-no-tc
  (:require [clojure.test :refer :all]
            [lambdaschmiede.kaocha-tc :refer [get-container]]))

(deftest no-tc-test-basic

  (testing "No Testcontainers initialized"

    (is (empty? (get-container :postgres-1)))
    (is (empty? (get-container :postgres-2)))
    (is (empty? (get-container :postgres-3)))))
