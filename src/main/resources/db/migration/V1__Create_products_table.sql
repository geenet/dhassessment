CREATE TABLE "products" (
  "id"        VARCHAR NOT NULL PRIMARY KEY,
  "model_id"  TEXT NOT NULL,
  "name"      TEXT NOT NULL,
  "brand"     TEXT NOT NULL,
  "price"     DOUBLE PRECISION NOT NULL,
  "media"     TEXT NOT NULL
);
