
(ns cumulo-server.updater.task (:require [cumulo-server.schema :as schema]))

(defn rm [db op-data state-id op-id op-time]
  (update db :tasks (fn [tasks] (dissoc tasks op-data))))

(defn add [db op-data state-id op-id op-time]
  (assoc-in db [:tasks op-id] (merge schema/task {:id op-id, :text op-data})))
