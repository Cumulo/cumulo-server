
(set-env!
 :dependencies '[[org.clojure/clojurescript "1.8.40"      :scope "test"]
                 [org.clojure/clojure       "1.8.0"       :scope "test"]
                 [adzerk/boot-cljs          "1.7.170-3"   :scope "test"]
                 [figwheel-sidecar        "0.5.2"   :scope "test"]
                 [com.cemerick/piggieback "0.2.1"   :scope "test"]
                 [org.clojure/tools.nrepl "0.2.10"  :scope "test"]
                 [ajchemist/boot-figwheel "0.5.2-2" :scope "test"]
                 [cirru/boot-cirru-sepal    "0.1.5"       :scope "test"]
                 [binaryage/devtools        "0.5.2"       :scope "test"]
                 [mrmcc3/boot-rev           "0.1.0"       :scope "test"]
                 [adzerk/boot-test          "1.1.1"       :scope "test"]
                 [mvc-works/hsl             "0.1.2"       :scope "test"]
                 [cumulo/shallow-diff       "0.1.1"]])

(require '[adzerk.boot-cljs   :refer [cljs]]
         '[cirru-sepal.core   :refer [transform-cirru cirru-sepal]]
         '[adzerk.boot-test   :refer :all]
         '[boot-figwheel])

(def +version+ "0.1.0")

(task-options!
  pom {:project     'mvc-works/boot-workflow
       :version     +version+
       :description "Workflow"
       :url         "https://github.com/mvc-works/boot-workflow"
       :scm         {:url "https://github.com/mvc-works/boot-workflow"}
       :license     {"MIT" "http://opensource.org/licenses/mit-license.php"}})

(refer 'boot-figwheel :rename '{cljs-repl fw-cljs-repl}) ; avoid some symbols

(task-options!
  figwheel {:build-ids  ["dev"]
           :all-builds [{:id "dev"
                         :compiler {:main 'cumulo-server.main
                                    :target :nodejs
                                    :source-map true
                                    :optimizations :none
                                    :output-to "app.js"
                                    :output-dir "server_out/"
                                    :verbose false}
                         :figwheel {:build-id  "dev"
                                    :on-jsload 'cumulo-server.main/on-jsload
                                    :heads-up-display true
                                    :autoload true
                                    :target :nodejs
                                    :debug false}}]
           :figwheel-options {:repl true
                              :http-server-root "target"
                              :reload-clj-files false
                              :load-warninged-code false}})

(deftask compile-cirru []
  (set-env!
    :source-paths #{"cirru/"})
  (comp
    (transform-cirru)
    (target :dir #{"compiled/"})))

(deftask watch-compile []
  (set-env!
    :source-paths #{"cirru/"})
  (comp
    (watch)
    (transform-cirru)
    (target :dir #{"compiled/"})))

(deftask dev []
  (set-env!
    :source-paths #{"compiled/src" "compiled/app"})
  (comp
    (repl)
    (figwheel)
    (target)))

(deftask build-simple []
  (set-env!
    :source-paths #{"cirru/src" "cirru/app"})
  (comp
    (transform-cirru)
    (cljs :compiler-options {:target :nodejs})
    (target)))

(deftask build-advanced []
  (set-env!
    :source-paths #{"cirru/src" "cirru/app"})
  (comp
    (transform-cirru)
    (cljs :optimizations :advanced :compiler-options {:target :nodejs})
    (target)))

(deftask build []
  (set-env!
    :source-paths #{"cirru/src"})
  (comp
    (transform-cirru)
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
    :source-paths #{"cirru/src" "cirru/test"})
  (comp
    (watch)
    (transform-cirru)
    (test :namespaces '#{boot-workflow.test})))
