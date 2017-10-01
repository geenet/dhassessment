package com.deliveryhero.restapi.services

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, Uri, _}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.deliveryhero.restapi.models.{ArticlesParserProtocol, FetchedPage}
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

case object Fetch
case object Info
case class InsertToDb(page: FetchedPage)
case class AllPages(f: Future[Seq[ByteString]])

trait ContentParserProtocol extends SprayJsonSupport with DefaultJsonProtocol with ArticlesParserProtocol {
  implicit val contentFormat = jsonFormat5(FetchedPage)
}

class FetcherActor(val fetcherService: FetcherService)(implicit ec: ExecutionContext, materializer: ActorMaterializer) extends Actor
  with ActorLogging with Directives with ContentParserProtocol {
  implicit private val system = context.system

  final val FetchUrl = Uri("https://api.zalando.com/articles")

  val fetchScheduleInterval: FiniteDuration = 2 minutes
  val infoScheduleInterval: FiniteDuration = 2 seconds

  val fetcherTask: Cancellable = context.system.scheduler.schedule(0 seconds, fetchScheduleInterval, self, Fetch)
  val infoTask: Cancellable = context.system.scheduler.schedule(0 seconds, infoScheduleInterval, self, Info)

  val paginationStream =
    asyncPageSource
    .map(fetcherService.insertToDb)

  private def asyncPageSource: Source[FetchedPage, NotUsed] = {
    val pageSize = 50

    Source.unfoldAsync(1L) { pageNum =>
      val futurePage: Future[FetchedPage] = fetchPage(pageNum, pageSize)

      val next = futurePage.map(pageData => if (pageData.page > pageData.totalPages) None else Some((pageNum + 1, pageData)))
      next
    }
  }

  def fetchPage(pageNum: Long, pageSize: Int): Future[FetchedPage] = {
    for {
      response: HttpResponse <- makeRequest(FetchUrl.withQuery(Uri.Query(
        "page" -> s"$pageNum",
        "pageSize" -> s"$pageSize"
      )))
      bytes: ByteString <- consumeResponse(response)
      pageOpt: Option[FetchedPage] <- decode(bytes)
    } yield pageOpt.get
  }

  def receive = {
    case Fetch =>
      log.info("Fetch started...")
      paginationStream.runWith(Sink.ignore)
    case Info =>
      fetcherService.count.map { elemCount =>
        log.info(s"Fetched $elemCount of elements.")
      }.recover {
        case ex =>
          log.error(s"Couldn't get inserted element count ${ex.getMessage}")
      }
  }

  def makeRequest(uri: Uri): Future[HttpResponse] =
    Http().singleRequest(HttpRequest(GET, uri = uri))

  def consumeResponse(resp: HttpResponse): Future[ByteString] = {
    /*
     * Need to consume entity stream in order to get body bytes
     */
    resp
      .entity
      .toStrict(2 seconds)
      .map(_.data)
  }

  def decode(bytes: ByteString): Future[Option[FetchedPage]] = {
    Unmarshal(bytes).to[FetchedPage].map { page =>
      Some(page)
    }.recover {
      case e: Exception =>
        log.error(s"Error during deserialization of response: ${e}")
        None
    }
  }
}
