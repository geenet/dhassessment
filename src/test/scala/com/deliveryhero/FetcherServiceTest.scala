package com.deliveryhero

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import com.deliveryhero.restapi.models.ProductEntity
import io.circe.generic.auto._
import org.scalatest.concurrent.ScalaFutures


class FetcherServiceTest extends BaseServiceTest with ScalaFutures {

  trait Context {
    val testProducts = populateProducts(5)
    val route = httpService.fetcherRouter.route
  }

  import fetcherService._

  "Fetcher Service" should {

    "get fetched products" in new Context {
      Get("/fetcher") ~> route ~> check {
        responseAs[Seq[ProductEntity]].isEmpty should be(false)
      }
    }

    "retrieve product by id" in new Context {
      val testProduct = testProducts(4).content.head.toProduct
      Get(s"/fetcher/${testProduct.id.get}") ~> route ~> check {
        responseAs[ProductEntity] should be(testProduct)
      }
    }

    "update product by id and retrieve it" in new Context {
      val testProduct = testProducts(4).content.head.toProduct
      val newProductName = randomString(10)
      val requestEntity = HttpEntity(MediaTypes.`application/json`,
        s"""{"id": "${testProduct.id.get}", "name": "${newProductName}"}""")

      Post(s"/fetcher/${testProduct.id.get}", requestEntity) ~> route ~> check {
        responseAs[ProductEntity] should be(testProduct.copy(name = newProductName))
        whenReady(getProductById(testProduct.id.get)) { result =>
          result.get.name should be(newProductName)
        }
      }
    }

    "delete product" in new Context {
      val testProduct = testProducts(4).content.head.toProduct

      Delete(s"/fetcher/${testProduct.id.get}") ~> route ~> check {
        response.status should be(NoContent)
        whenReady(getProductById(testProduct.id.get)) { result =>
          result should be(None: Option[ProductEntity])
        }
      }
    }
  }
}
