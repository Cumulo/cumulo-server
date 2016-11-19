
(ns cumulo-server.view )

(defn render-view [state-id db]
  {:states (get-in db [:states state-id]), :tasks (:tasks db)})

(defn render-scene [db] db)
