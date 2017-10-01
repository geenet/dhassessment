package com.deliveryhero.restapi.http

import akka.http.scaladsl.server.Directives._
import com.deliveryhero.restapi.http.routes.{FetcherServiceRoute, ServingServiceRoute}
import com.deliveryhero.restapi.services.{FetcherService, ServingService}
import com.deliveryhero.restapi.utils.CorsSupport

import scala.concurrent.ExecutionContext

class HttpService(fetcherService: FetcherService,
                  servingService: ServingService
                 )(implicit executionContext: ExecutionContext) extends CorsSupport {

  val fetcherRouter = new FetcherServiceRoute(fetcherService)
  val servingRouter = new ServingServiceRoute(servingService)(fetcherService)

  val routes =
    pathPrefix("v1") {
      corsHandler {
        fetcherRouter.route ~
        servingRouter.route
      }
    }

}
