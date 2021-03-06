
(ns cumulo-server.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.reader :as reader]
            [shallow-diff.diff :refer [diff]]
            [cljs.core.async :refer [chan >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce socket-registry (atom {}))

(defonce server-chan (chan))

(def shortid (js/require "shortid"))

(def ws (js/require "ws"))

(def WebSocketServer (.-Server ws))

(defn handle-message [op op-data state-id]
  (let [op-id (.generate shortid), op-time (.valueOf (js/Date.))]
    (go (>! server-chan [op op-data state-id op-id op-time]))))

(defn run-server! [configs]
  (let [wss (new WebSocketServer (js-obj "port" (:port configs)))]
    (.on
     wss
     "connection"
     (fn [socket]
       (let [state-id (.generate shortid)]
         (handle-message :state/connect nil state-id)
         (swap! socket-registry assoc state-id socket)
         (.on
          socket
          "message"
          (fn [rawData]
            (let [action (reader/read-string rawData), [op op-data] action]
              (handle-message op op-data state-id))))
         (.on
          socket
          "close"
          (fn []
            (swap! socket-registry dissoc state-id)
            (handle-message :state/disconnect nil state-id)))))))
  server-chan)

(defonce client-caches (atom {}))

(defn render-clients! [db render-scene render-view]
  (doseq [state-entry (:states db)]
    (let [state-id (first state-entry)
          scene (render-scene db)
          new-store (render-view state-id scene)
          old-store (or (get @client-caches state-id) {})
          changes (diff old-store new-store)
          socket (get @socket-registry state-id)]
      (if (and (not= changes []) (some? socket))
        (do (.send socket (pr-str changes)) (swap! client-caches assoc state-id new-store))))))
