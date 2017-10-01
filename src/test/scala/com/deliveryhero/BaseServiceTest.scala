package com.deliveryhero

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.deliveryhero.restapi.http.HttpService
import com.deliveryhero.restapi.models.{Article, Brand, FetchedPage, Image, Media, Price, Units}
import com.deliveryhero.restapi.services.{FetcherService, ServingService}
import com.deliveryhero.restapi.utils.DatabaseService
import com.deliveryhero.utils.InMemoryPostgresStorage._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

trait BaseServiceTest extends WordSpec with Matchers with ScalatestRouteTest with FailFastCirceSupport {

  dbProcess.getProcessId

  private val databaseService = new DatabaseService(jdbcUrl, dbUser, dbPassword)

  val fetcherService = new FetcherService(databaseService)
  val servingService = new ServingService(databaseService)
  val httpService = new HttpService(fetcherService, servingService)

  def randomString(length: Int) = Random.alphanumeric.take(length).mkString

  def populateProducts(size: Int): Seq[FetchedPage] = {
    val savedUsers = (1 to size).map { _ =>
      FetchedPage(
        content = Article(
          id = Some(randomString(10)),
          modelId = randomString(10),
          name = randomString(10),
          brand = Brand(randomString(10)),
          units = Units(Price(Random.nextDouble())) :: Nil,
          media = Media(Image(randomString(10)) :: Nil)
        ) :: Nil,
        totalElements = 0L,
        totalPages = 0L,
        page = 0L,
        size = 0L
      )
    }.map(fetcherService.insertToDb)

    Await.result(Future(savedUsers), 10.seconds)
  }
}
