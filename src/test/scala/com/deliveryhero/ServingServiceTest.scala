package com.deliveryhero

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import com.deliveryhero.restapi.http.pagination.{Page, PaginatedResult, PaginationTotals}
import com.deliveryhero.restapi.models.ProductEntity
import io.circe.generic.auto._
import org.scalatest.concurrent.ScalaFutures
import akka.http.scaladsl.model.StatusCodes._


class ServingServiceTest extends BaseServiceTest with ScalaFutures {

  import fetcherService._

  trait Context {
    val testProducts = populateProducts(100)
    val route = httpService.servingRouter.route
  }

  "Serving Service" should {
    "get products" in new Context {
      Get("/api") ~> route ~> check {
        response.status shouldNot be(NotFound)
        noException shouldBe thrownBy {
          responseAs[PaginatedResult[ProductEntity]] shouldNot be(null)
        }
      }
    }

    "retrieve product by id" in new Context {
      val testProduct = testProducts(4).content.head.toProduct
      Get(s"/api/${testProduct.id.get}") ~> route ~> check {
        responseAs[ProductEntity] should be(testProduct)
      }
    }

    "update product by id and retrieve it" in new Context {
      val testProduct = testProducts(4).content.head.toProduct
      val newProductName = randomString(10)
      val requestEntity = HttpEntity(MediaTypes.`application/json`,
        s"""{"id": "${testProduct.id.get}", "name": "${newProductName}"}""")

      Post(s"/api/${testProduct.id.get}", requestEntity) ~> route ~> check {
        responseAs[ProductEntity] should be(testProduct.copy(name = newProductName))
        whenReady(getProductById(testProduct.id.get)) { result =>
          result.get.name should be(newProductName)
        }
      }
    }

    "delete product" in new Context {
      val testProduct = testProducts(4).content.head.toProduct

      Delete(s"/api/${testProduct.id.get}") ~> route ~> check {
        response.status should be(NoContent)
        whenReady(getProductById(testProduct.id.get)) { result =>
          result should be(None: Option[ProductEntity])
        }
      }
    }

    "search according to given criterias" in new Context {
      val testProduct = testProducts(4).content.head.toProduct
      val perPage = 5
      val name = testProduct.name

      Get(s"/api/search?q=${name}&per_page=${perPage}&sort=price&direction=asc") ~> route ~> check {
        responseAs[PaginatedResult[ProductEntity]] should be {
          PaginatedResult[ProductEntity](elements = Vector {testProduct}, Page(1, perPage),
            Some(PaginationTotals(1, 1)))
        }
      }
    }
  }
}
