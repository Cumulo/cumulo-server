
(set-env!
 :dependencies '[[org.clojure/clojure       "1.8.0"       :scope "test"]
                 [org.clojure/clojurescript "1.9.293"     :scope "test"]
                 [org.clojure/core.async    "0.2.385"     :scope "test"]
                 [adzerk/boot-cljs          "1.7.228-1"   :scope "test"]
                 [figwheel-sidecar          "0.5.4-5"     :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"       :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.12"      :scope "test"]
                 [ajchemist/boot-figwheel   "0.5.4-5"     :scope "test"]
                 [cirru/boot-stack-server   "0.1.23"      :scope "test"]
                 [adzerk/boot-test          "1.1.2"       :scope "test"]
                 [cumulo/recollect          "0.1.0"]
                 [cumulo/shallow-diff       "0.1.1"]])

(require '[adzerk.boot-cljs   :refer [cljs]]
         '[stack-server.core  :refer [start-stack-editor! transform-stack]]
         '[adzerk.boot-test   :refer :all]
         '[boot-figwheel])

(def +version+ "0.1.1")

(task-options!
  pom {:project     'cumulo/server
       :version     +version+
       :description "Cumulo server runner"
       :url         "https://github.com/Cumulo/cumulo-server"
       :scm         {:url "https://github.com/Cumulo/cumulo-server"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(refer 'boot-figwheel :rename '{cljs-repl fw-cljs-repl}) ; avoid some symbols

(def all-builds
  [{:id "dev"
    :source-paths ["src"]
    :compiler {:output-to "app.js"
               :output-dir "server_out/"
               :main 'cumulo-server.main
               :target :nodejs
               :optimizations :none
               :source-map true}
    :figwheel {:build-id  "dev"
               :on-jsload 'cumulo-server.main/on-jsload
               :autoload true
               :debug false}}])

(def figwheel-options
  {:repl true
   :http-server-root "target"
   :reload-clj-files false})

(deftask editor! []
  (comp
    (repl)
    (start-stack-editor! :port 7011)
    (target :dir #{"src/"})))

(deftask generate-code []
  (comp
    (transform-stack :filename "stack-sepal.ir")
    (target :dir #{"src/"})))

(deftask dev []
  (set-env!
    :source-paths #{"src"})
  (comp
    (repl)
    (figwheel
      :build-ids ["dev"]
      :all-builds all-builds
      :figwheel-options figwheel-options
      :target-path "target")
    (target)))

(deftask build-advanced []
  (set-env!
    :asset-paths #{"assets/"})
  (comp
    (transform-stack :filename "stack-sepal.ir")
    (cljs :optimizations :advanced
          :compiler-options {:language-in :ecmascript5
                             :target :nodejs
                             :pseudo-names true
                             :static-fns true
                             :parallel-build true
                             :optimize-constants true
                             :source-map true})
    (target)))

(deftask build []
  (comp
    (transform-stack :filename "stack-sepal.ir")
    (pom)
    (jar)
    (install)
    (target)))

(deftask deploy []
  (set-env!
    :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))
  (comp
    (build)
    (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))

(deftask watch-test []
  (set-env!
    :source-paths #{"src" "test"})
  (comp
    (watch)
    (test :namespaces '#{cumulo-server.test})))
