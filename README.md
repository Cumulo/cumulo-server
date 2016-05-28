
Cumulo Server
----

Use it togather with `cumulo/client`.

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/cumulo/server.svg)](https://clojars.org/cumulo/server)

```clojure
[cumulo/server "0.1.0"]
```

```clojure
(defn updater [db op op-data state-id op-id op-time] db)
(defn render-scene [db] db)
(defn render-view [state-id scene] {state-id {}})
(def configs {:port 4010})

(cumulo-server.core/setup-server! db-ref updater render-scene render-view configs)
(cumulo-server.core/reload-renderer! db updater render-scene render-view)
```

### Develop

Based on https://github.com/mvc-works/boot-workflow

```bash
boot dev
# watching for the REPL
(start-figwheel!)
# new session
cd target/
node app.js
```

### License

MIT
