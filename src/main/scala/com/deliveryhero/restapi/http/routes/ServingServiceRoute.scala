package com.deliveryhero.restapi.http.routes

import akka.http.scaladsl.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import com.deliveryhero.restapi.http.pagination.{Page, PaginationRequest}
import com.deliveryhero.restapi.models.{ProductEntity, ProductEntityUpdate}
import com.deliveryhero.restapi.services.{FetcherService, ServingService}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}

class ServingServiceRoute(val servingService: ServingService)(fetcherService: FetcherService)(implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {

  import StatusCodes._
  import fetcherService._
  import servingService._

  val route = pathPrefix("api" / "search") {
    get {
      parameterMap { (params: Map[String, String]) =>
        complete(search(params).map(_.asJson))
      }
    }
  } ~ pathPrefix("api") {
    pathEndOrSingleSlash {
      get {
        parameters('page.as[Long].?, 'per_page.as[Long].?) { (page, perPage) =>
          val requestedPage = Page(page.getOrElse(1L), perPage.getOrElse(10L))
          complete(pagination(getProductQuery(), PaginationRequest(requestedPage)).map(_.asJson))
        }
      }
    } ~
      pathPrefix(Segment) { (id: String) =>
        pathEndOrSingleSlash {
          get {
            onSuccess(getProductById(id)) {
              case v@Some(_) => complete(v.asJson)
              case _ => complete(NotFound)
            }
          } ~
            post {
              entity(as[ProductEntityUpdate]) { productUpdate =>
                complete(updateProduct(id, productUpdate).map(_.asJson))
              }
            } ~
            delete {
              onSuccess(deleteProduct(id)) { _ =>
                complete(NoContent)
              }
            }
        }
      }
  }
}
