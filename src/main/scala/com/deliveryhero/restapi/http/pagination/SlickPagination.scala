package com.deliveryhero.restapi.http.pagination

import com.deliveryhero.restapi.utils.DatabaseService


trait SlickPagination {
  this: {
    val databaseService: DatabaseService
  } =>

  import databaseService.profile.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit class PaginationExtensionMethods[T, U](query: Query[T, U, Seq]) {

    def paginatedQuery(page: Page) = query.drop((page.number - 1) * page.size).take(page.size)

    def paginated(request: PaginationRequest) = (paginatedQuery(request.page).result zip pageTotals(request)) map (PaginatedResult[U](_: Seq[U], request.page, _: Option[PaginationTotals])).tupled

    def pageTotals(request: PaginationRequest) = if (request.returnTotals) query.size.result map { c => Some(PaginationTotals(c, math.ceil(c.toDouble / request.page.size).toLong)) } else DBIO.successful(None)
  }
}
