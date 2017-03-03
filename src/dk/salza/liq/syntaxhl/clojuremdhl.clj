(ns dk.salza.liq.syntaxhl.clojuremdhl
  (:use [dk.salza.liq.slider :as slider :exclude [create]]))

(defn look-ahead
  [sl n]
  (apply str (map get-char (take n (iterate #(right % 1) sl)))))

(defn next-face
  [face ch pch ppch sl]
  (cond (= face :string)  (cond (and (= pch "\"") (= ppch "\\")) face
                                (and (= pch "\"") (string? (-> sl (left 2) (get-char)))) :plain
                                (and (= pch "\"") (re-matches #"[^#\( \[{\n]" ppch)) :plain
                                (and (= pch "\"") (re-matches #"[\)\]}]" (or ch " "))) :plain
                                :else face)
        (= face :plain)   (cond (and (= ch "\"") (re-matches #"[#\( \[{\n]" pch)) :string
                                (= ch ";") :comment
                                (and (= ch "#") (or (= pch "\n") (= pch "") (= (get-point sl) 0))) :comment 
                                (and (= pch "(") (re-find #"def(n|n-|test|record|protocol)? " (look-ahead sl 13))) :type1
                                (and (= ch ":") (re-matches #"[\( \[{\n]" pch)) :type3
                                :else face)
        (= face :type1)   (cond (= ch " ") :type2
                                :else face)
        (= face :type2)   (cond (= ch " ") :plain
                                :else face)
        (= face :type3)   (cond (re-matches #"[\)\]}\s]" (or ch " ")) :plain
                                :else face)
        (= face :comment) (cond (= ch "\n") :plain
                                :else face)
                          :else face))