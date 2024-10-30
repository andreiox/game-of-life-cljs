(ns game-of-life-cljs.core
  (:require [reagent.core :as r]
            [reagent.dom :as d]))

;; -------------------------
;; Game Config

(defonce cell-size 7)

(defonce width 50)
(defonce height 50)

(defonce cells-amount
  (* width height))

(defonce grid-width
  (str (* width cell-size) "px"))

(defonce update-rate-ms 300)

;; -------------------------
;; Game Logic

(def game-state
  (r/atom (vec (take cells-amount (repeatedly #(= 0 (mod (rand-int 100) 4)))))))

(defn next-generation
  [index cells]
  (let [;cells @game-state
        cell (get cells index)
        neighbours [(get cells (- index width 1))
                    (get cells (- index width))
                    (get cells (- index width -1))
                    (get cells (- index 1))
                    (get cells (+ index -1))
                    (get cells (+ index width -1))
                    (get cells (+ index width))
                    (get cells (+ index width 1))]
        alive-neighbours (count (filter true? neighbours))]
    (cond
      (and cell (< alive-neighbours 2)) false
      (and cell (> alive-neighbours 3)) false
      (and cell (or (= alive-neighbours 2) (= alive-neighbours 3))) true
      (and (not cell) (= alive-neighbours 3)) true
      :else false)))

(def update-state
  (js/setInterval #(let [game @game-state]
                     (reset! game-state (mapv (fn [index] (next-generation index game)) (range cells-amount))))
                  update-rate-ms))

;; -------------------------
;; Views

(defn generate-cell
  [alive?]
  [:div {:key (random-uuid)
         :style {:width (str cell-size "px")
                 :height (str cell-size "px")
                 :background-color (if alive? "black" "white")}}])

(defn generate-row
  [cells]
  [:div {:key (random-uuid)
         :style {:display "flex"}}
   (map generate-cell cells)])

(defn generate-grid
  []
  [:div {:style {:border "1px solid black" :width grid-width}}
   (map generate-row (partition-all width @game-state))])

(defn home-page []
  (js/console.log "generatin home-page")
  [:div
   [:h2 "Game of Life"]
   [:p "This is a simple implementation of Conway's Game of Life in ClojureScript using Reagent."]
   [generate-grid]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
