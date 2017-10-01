# Assessment

Application contains 3 routes:

* `/v1/api` - For CRUD operations on products with pagination on read
* `/v1/fetcher` – CRUD operations on fetched products with not paginated access
* `/v1/api/search` – Search feature route for products with pagination

# Running

You can either use:

```bash
$ sbt run
```

inside of the project with Postgres running locally.

OR using Docker:

```bash
$ sbt docker:publishLocal
$ docker-compose up --build
```

**NOTE:** For the sake of testing application drops `products` table in every start-up and recreates it.

# Testing

You can run:

```bash
$ sbt test
```

for running tests.

# API Documentation

## Fetcher API

* GET `/v1/fetcher`
will bulk load all inserted products

* GET `/v1/fetcher/{id}`
will fetch product with given id

* POST `/v1/fetcher` with:
```json
{
    "id": "FA141J017-A11",
    "modelId": "DODGE",
    "name": "RICK",
    "brand": "REVERB",
    "price": 90.0,
    "media": "http://localhost:9000/v1/api/FA141J017-A11"
}
```
will update given ID with data posted.

* DELETE `/v1/fetcher/{id}`
will delete product that matches given id from database

## Serving API CRUD

* GET `/v1/api`
will give paginated results (default 10 elements per page)
You can pass `per_page` and `page` as query parameters

* GET `/v1/api/{id}`
will fetch product with given id

* POST `/v1/api` with:
```json
{
    "id": "FA141J017-A11",
    "modelId": "DODGE",
    "name": "RICK",
    "brand": "REVERB",
    "price": 90.0,
    "media": "http://localhost:9000/v1/api/FA141J017-A11"
}
```
will update given ID with data posted.

* DELETE `/v1/api/{id}`
will delete product that matches given id from database

## Serving Search API

Behaves exactly defined in the task
Example url can be:

```
http://localhost:9000/v1/api/search?q=jeans&page=3&per_page=5&sort=price&direction=asc
```
