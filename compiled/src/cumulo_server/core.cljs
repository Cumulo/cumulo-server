
(ns cumulo-server.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a :refer [>! <! chan]]
            [cljs.reader :as reader]
            [shallow-diff.diff :refer [diff]]))

(defonce shortid (js/require "shortid"))

(defonce ws (js/require "ws"))

(defonce WebSocketServer (.-Server ws))

(defonce socket-registry (atom {}))

(defonce client-caches (atom {}))

(defonce updater-ref (atom (fn [op op-data state-id op-id op-time] db)))

(defonce scene-ref (atom (fn [db] db)))

(defonce view-ref (atom (fn [state-id scene] {state-id {}})))

(defn rerender-view [db]
  (go
    (doseq [state-entry (:states db)]
      (let [state-id (first state-entry)
            scene (@render-scene db)
            new-store (@render-view state-id scene)
            old-store (or (get @client-caches state-id) {})
            changes (differ/diff old-store new-store)
            socket (get @socket-registry state-id)]
        (if (and (not= changes []) (some? socket))
          (do
            (.send socket (pr-str changes))
            (swap! client-caches assoc state-id new-store)))))))

(defn handle-message [db-ref op op-data state-id op-id op-time]
  (let [op-id (.generate shortid)
        op-time (.valueOf (js/Date.))
        new-db (@updater-ref
                 @db-ref
                 op
                 op-data
                 state-id
                 op-id
                 op-time)]
    (reset! db-ref new-db)
    (rerender-view @db-ref)))

(defn reload-renderer! [updater render-scene render-view]
  (reset! updater-ref updater)
  (reset! scene-ref render-scene)
  (reset! view-ref render-view)
  (rerender-view))

(defn setup-server! [db-ref updater render-scene render-view configs]
  (let [wss (new WebSocketServer (js-obj "port" (:port configs)))]
    (reset! updater-ref updater)
    (reset! scene-ref render-scene)
    (reset! view-ref render-view)
    (.on
      wss
      "connection"
      (fn [socket]
        (let [state-id (.generate shortid)]
          (handle-message db-ref :state/connect nil)
          (swap! socket-registry assoc state-id socket)
          (.on
            socket
            "message"
            (fn [rawData]
              (let [action (reader/read-string rawData)
                    [op op-data] action]
                (handle-message db-ref op op-data state-id))))
          (.on
            socket
            "close"
            (fn []
              (swap! socket-registry dissoc state-id)
              (println "socket close" state-id)
              (handle-message
                db-ref
                :state/disconnect
                nil
                state-id))))))))
