// init-mongo.js
db = db.getSiblingDB('studs_db');
db.counters.insertOne({ _id: "space_marine_id", seq: 1000 });
db.counters.insertOne({ _id: "space_marine_id", seq: 1000 });