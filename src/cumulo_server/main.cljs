
(ns cumulo-server.main
  (:require [cljs.nodejs :as nodejs]
            [cumulo-server.schema :as schema]
            [cumulo-server.core :refer [run-server! render-clients!]]
            [cumulo-server.updater.core :refer [updater]]
            [cumulo-server.view :refer [render-view render-scene]]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defonce writer-db-ref (atom schema/database))

(defonce reader-db-ref (atom @writer-db-ref))

(defn on-jsload []
  (println "code updated")
  (render-clients! @reader-db-ref render-scene render-view))

(defn render-loop! []
  (if (not (identical? @reader-db-ref @writer-db-ref))
    (do
     (reset! reader-db-ref @writer-db-ref)
     (render-clients! @reader-db-ref render-scene render-view)))
  (js/setTimeout render-loop! 300))

(defn -main []
  (nodejs/enable-util-print!)
  (let [server-ch (run-server! {:port 4010})]
    (go-loop
     []
     (let [[op op-data state-id op-id op-time] (<! server-ch)
           new-db (updater @writer-db-ref op op-data state-id op-id op-time)]
       (reset! writer-db-ref new-db)
       (recur))))
  (render-loop!)
  (add-watch reader-db-ref :log (fn [] (println @reader-db-ref)))
  (println "server started"))

(set! *main-cli-fn* -main)
