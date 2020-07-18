(ns osm.core
  (:require [clj-http.client :as client]
            [clojure.string :as s])
  (:use [seesaw.core :only [native! show! vertical-panel horizontal-panel frame label]])
  (:gen-class))

(def latitude (Double/parseDouble (first *command-line-args*)))
(def longitude (Double/parseDouble (second *command-line-args*)))
(def zoom (Integer/parseInt (last *command-line-args*)))

;; This API returns map images. We need to pass it longitude, latitude and zoom.
(def api-url "https://a.tile.openstreetmap.org/")

(native!)

;; The OSM API does not accept latitude and longitude directly.
;; They must be converted with the formula specified here: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
(defn tile [lat lon zoom]
  (let [zoom-shifted (bit-shift-left 1 zoom)
        lat-radians (Math/toRadians lat)
        xtile (int (Math/floor (* (/ (+ 180 lon) 360) zoom-shifted)))
        ytile (int (Math/floor (* (/ (- 1
                                        (/
                                         (Math/log (+ (Math/tan lat-radians)
                                                      (/ 1 (Math/cos lat-radians))))
                                         Math/PI))
                                     2)
                                  zoom-shifted)))]
    {:z zoom
     :x (cond (< xtile 0) 0
              (>= xtile zoom-shifted) (- zoom-shifted 1)
              :else xtile)
     :y (cond (< ytile 0) 0
              (>= ytile zoom-shifted) (- zoom-shifted 1)
              :else ytile)}))

;; Small function to help glue the final URL together.
(defn tile-str [{:keys [x y z]}]
  (str z "/" x "/" y))

(defn make-get
  "Makes an async HTTP get request to the provided URL (accepts a configuration hash which is passed to clj-http)."
  [url config]
  (future (:body (client/get url config))))

(defn read-img-from-stream
  "Contract: InputStream -> java.awt.image.BufferedImage
  Tries to return an image by reading the contents of img-stream.
  This function can throw an exception if given a bad stream."
  [img-stream]
  (javax.imageio.ImageIO/read img-stream))

(defn make-main-window [child-widgets]
  (frame :title "OSM Map."
         :height 800
         :width 800
         :content child-widgets
         :on-close :exit))

(defn -main
  [& args]
  ;; NOTE: The OSM api will not give us the map as a single image.
  ;; Instead we have to put it together from 256x256 pieces.
  (let [tile1 (tile latitude longitude zoom)
        tile2 (update-in tile1 [:x] dec)
        tile3 (update-in tile1 [:x] inc)
        tile4 (update-in tile2 [:y] dec)
        tile5 (update-in tile1 [:y] dec)
        tile6 (update-in tile3 [:y] dec)
        tile7 (update-in tile2 [:y] inc)
        tile8 (update-in tile1 [:y] inc)
        tile9 (update-in tile3 [:y] inc)
        img1 (make-get (str api-url (tile-str tile1) ".png") {:as :stream})
        img2 (make-get (str api-url (tile-str tile2) ".png") {:as :stream})
        img3 (make-get (str api-url (tile-str tile3) ".png") {:as :stream})
        img4 (make-get (str api-url (tile-str tile4) ".png") {:as :stream})
        img5 (make-get (str api-url (tile-str tile5) ".png") {:as :stream})
        img6 (make-get (str api-url (tile-str tile6) ".png") {:as :stream})
        img7 (make-get (str api-url (tile-str tile7) ".png") {:as :stream})
        img8 (make-get (str api-url (tile-str tile8) ".png") {:as :stream})
        img9 (make-get (str api-url (tile-str tile9) ".png") {:as :stream})
        map-label1 (label :icon (read-img-from-stream @img1))
        map-label2 (label :icon (read-img-from-stream @img2))
        map-label3 (label :icon (read-img-from-stream @img3))
        map-label4 (label :icon (read-img-from-stream @img4))
        map-label5 (label :icon (read-img-from-stream @img5))
        map-label6 (label :icon (read-img-from-stream @img6))
        map-label7 (label :icon (read-img-from-stream @img7))
        map-label8 (label :icon (read-img-from-stream @img8))
        map-label9 (label :icon (read-img-from-stream @img9))
        main-frame (make-main-window
                     (vertical-panel
                       :items [(horizontal-panel :items [map-label4 map-label5 map-label6])
                               (horizontal-panel :items [map-label2 map-label1 map-label3])
                               (horizontal-panel :items [map-label7 map-label8 map-label9])]))]
    (show! main-frame)))
