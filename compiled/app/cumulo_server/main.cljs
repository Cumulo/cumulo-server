
(ns cumulo-server.main
  (:require [cljs.nodejs :as nodejs]
            [cumulo-server.schema :as schema]
            [cumulo-server.core :refer [setup-server! reload-renderer!]]
            [cumulo-server.updater.core :refer [updater]]
            [cumulo-server.view :refer [render-view render-scene]]))

(defonce db-ref (atom schema/database))

(defn -main []
  (nodejs/enable-util-print!)
  (setup-server! db-ref updater render-scene render-view {:port 4010})
  (println "server started"))

(set! *main-cli-fn* -main)

(defn on-jsload [] (reload-renderer! updater render-scene render-view))
