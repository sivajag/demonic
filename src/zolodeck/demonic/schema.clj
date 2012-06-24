(ns zolodeck.demonic.schema
  (:use [datomic.api :only [tempid] :as db]
        zolodeck.utils.debug))

(def SCHEMA-MAP (atom {}))

;; declaration

(defn fact-schema [attribute value-type cardinality fulltext? doc]
  (let [schema {:db/id (db/tempid :db.part/db)
                :db/ident attribute
                :db/valueType value-type
                :db/cardinality cardinality
                :db/fulltext fulltext?
                :db/doc doc
                :db.install/_attribute :db.part/db}]
    (swap! SCHEMA-MAP assoc attribute schema)
    schema))

(defn string-fact-schema [attribute fulltext? doc]
  (fact-schema attribute :db.type/string :db.cardinality/one fulltext? doc))

(defn strings-fact-schema [attribute fulltext? doc]
  (fact-schema attribute :db.type/string :db.cardinality/many fulltext? doc))

(defn instant-fact-schema [attribute fulltext? doc]
  (fact-schema attribute :db.type/instant :db.cardinality/one fulltext? doc))

(defn uuid-fact-schema [attribute fulltext? doc]
  (fact-schema attribute :db.type/uuid :db.cardinality/one fulltext? doc))

(defn refs-fact-schema [attribute fulltext? doc]
  (fact-schema attribute :db.type/ref :db.cardinality/many fulltext? doc))

(defn ref-fact-schema [attribute fulltext? doc]
  (fact-schema attribute :db.type/ref :db.cardinality/one fulltext? doc))

;; introspection

(defn is-ref? [attribute]
  (-> (get-in @SCHEMA-MAP [attribute :db/valueType])
      (= :db.type/ref)))

(defn cardinality [attribute]
  (get-in @SCHEMA-MAP [attribute :db/cardinality]))

(defn is-cardinality-many? [attribute]
  (= (cardinality attribute) :db.cardinality/many))

(defn is-cardinality-one? [attribute]
  (= (cardinality attribute) :db.cardinality/one))

(defn is-string? [attribute]
  (and (= :db.type/string (get-in @SCHEMA-MAP [attribute :db/valueType]))
       (= :db.cardinality/one (get-in @SCHEMA-MAP [attribute :db/cardinality]))))

(defn is-strings? [attribute]
  (and (= :db.type/string (get-in @SCHEMA-MAP [attribute :db/valueType]))
       (= :db.cardinality/many (get-in @SCHEMA-MAP [attribute :db/cardinality]))))

(defn is-multiple-ref-attrib? [k]
  (and (is-ref? k) (is-cardinality-many? k)))

(defn is-single-ref-attrib? [k]
  (and (is-ref? k) (is-cardinality-one? k)))

(defn has-refs? [a-map]
  (->> a-map keys (some is-ref?)))