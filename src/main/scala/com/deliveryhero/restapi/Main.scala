package com.deliveryhero.restapi

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.deliveryhero.restapi.http.HttpService
import com.deliveryhero.restapi.services.{FetcherActor, FetcherService, ServingService}
import com.deliveryhero.restapi.utils.{Config, DatabaseService, FlywayService}

import scala.concurrent.ExecutionContext

object Main extends App with Config {
  implicit val actorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.dropDatabase()
  flywayService.migrateDatabaseSchema()

  val databaseService = new DatabaseService(jdbcUrl, dbUser, dbPassword)

  val fetcherService = new FetcherService(databaseService)
  val servingService = new ServingService(databaseService)

  val httpService = new HttpService(fetcherService, servingService)

  val fetcherActor = actorSystem.actorOf(Props(new FetcherActor(fetcherService)), "fetcher-actor")

  Http().bindAndHandle(httpService.routes, httpHost, httpPort)
}
