
(ns cumulo-server.schema )

(def database {:states {}, :tasks {}, :users {}})

(def task {:done? false, :id nil, :text ""})

(def state {:user-id nil, :id nil})
