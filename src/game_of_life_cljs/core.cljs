(ns game-of-life-cljs.core
  (:require [reagent.core :as r]
            [reagent.dom :as d]))

;; -------------------------
;; Game Config

(defonce cell-size 7)

(defonce width 40)
(defonce height 40)

(defonce grid-width
  (str (* width cell-size) "px"))

(defonce update-rate-ms 100)

;; -------------------------
;; Game Logic

(defn get-cell
  [row column game-state]
  (-> game-state
      (get row)
      (get column)))

(defn generate-row-game-state
  [_]
  (vec (take width (repeatedly #(= 0 (mod (rand-int 100) 7))))))

(def game-state
  (r/atom (mapv generate-row-game-state (range height))))

(defn next-generation
  [row-number index game-state]
  (let [cell (get-cell row-number index game-state)
        neighbours [(get-cell (dec row-number) (dec index) game-state)
                    (get-cell (dec row-number) index game-state)
                    (get-cell (dec row-number) (inc index) game-state)

                    (get-cell row-number (dec index) game-state)
                    (get-cell row-number (inc index) game-state)

                    (get-cell (inc row-number) (dec index) game-state)
                    (get-cell (inc row-number) index game-state)
                    (get-cell (inc row-number) (inc index) game-state)]
        alive-neighbours (count (filter true? neighbours))]
    (cond
      (and cell (< alive-neighbours 2)) false
      (and cell (> alive-neighbours 3)) false
      (and cell (or (= alive-neighbours 2) (= alive-neighbours 3))) true
      (and (not cell) (= alive-neighbours 3)) true
      :else false)))

(def update-state
  (js/setInterval #(let [game @game-state
                         new-generation (mapv (fn [row] (mapv (fn [column] (next-generation row column game)) (range width))) (range height))]
                     (reset! game-state new-generation))
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
   (map generate-row @game-state)])

(defn home-page []
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
