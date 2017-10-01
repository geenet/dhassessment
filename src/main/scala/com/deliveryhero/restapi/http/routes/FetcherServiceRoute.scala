package com.deliveryhero.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.deliveryhero.restapi.models.ProductEntityUpdate
import com.deliveryhero.restapi.services.FetcherService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

class FetcherServiceRoute(val fetcherService: FetcherService)(implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {

  import StatusCodes._
  import fetcherService._

  val route = pathPrefix("fetcher") {
    pathEndOrSingleSlash {
      get {
        complete(getProducts().map(_.asJson))
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
