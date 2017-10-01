package com.deliveryhero

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import com.deliveryhero.restapi.Main.fetcherService
import com.deliveryhero.restapi.services.{Fetch, FetcherActor}
import org.scalatest.{FlatSpecLike, Matchers}
import org.scalatest.concurrent.{Eventually, ScalaFutures}

import scala.concurrent.Future
import scala.concurrent.duration._

class FetcherActorTest extends BaseServiceTest
  with Matchers
  with ScalaFutures
  with Eventually {

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  import fetcherService._

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = 2 minutes, interval = 5 seconds)

  "Fetcher Actor" should {
    "fetch data from external API" in {
      val ref = system.actorOf(Props(new FetcherActor(fetcherService)), "fetcher-actor")

      // It will be started with scheduler immediately but let's be explicit...
      ref ! Fetch

      val fetch = Future {
        Thread.sleep((30 seconds).toMillis)
        count
      }

      whenReady(fetch) { countF =>
        whenReady(countF) { elemCount =>
          elemCount should be >= 500
        }
      }
    }
  }
}
