(ns zolodeck.demonic.core
  (:use [datomic.api :only [q db] :as db]
        zolodeck.demonic.loadable
        zolodeck.demonic.helper        
        zolodeck.demonic.refs
        zolodeck.utils.debug))

(defmacro in-demarcation [& body]
  `(run-in-demarcation (fn [] ~@body)))

(defn wrap-demarcation [handler]
  (fn [request]
    (if-not DATOMIC-TEST
      (in-demarcation (handler request))
      (handler request))))

(defn init-db [datomic-db-name datomic-schema]
  (initialize-datomic datomic-db-name datomic-schema))

(defn delete-db [datomic-db-name]
  (db/delete-database datomic-db-name))

(defn run-query [query & extra-inputs]
  (apply q query @DATOMIC-DB extra-inputs))

(defn load-entity [eid]
  (let [e (load-from-db eid)]
    (when (:db/id e)
      (-> e entity->loadable))))

(defn load-and-transform-with [eid transform]
  (-> eid load-entity transform))

(defn insert [a-map]
  (when a-map
    (-> a-map with-demonic-attributes process-ref-attributes run-transaction))
  a-map)

(defn insert-and-transform-with [a-map transform]
  (-> a-map insert transform))

(defn append [entity attrib value-entities]
  (let [with-attribs (map with-demonic-attributes value-entities)
        append-txn (append-ref-txn entity attrib with-attribs)]
    (run-transaction (conj with-attribs append-txn))))

(defn delete [entity]
  (-> entity retract-entity-txn vector run-transaction))
