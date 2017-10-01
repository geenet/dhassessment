package com.deliveryhero.restapi.services

import com.deliveryhero.restapi.http.pagination.{Page, PaginationRequest, SlickPagination}
import com.deliveryhero.restapi.models.ProductEntity
import com.deliveryhero.restapi.models.db.ProductEntityTable
import com.deliveryhero.restapi.utils.DatabaseService

import scala.concurrent.ExecutionContext

class ServingService(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext)
  extends ProductEntityTable with SlickPagination {

  import databaseService._
  import databaseService.profile.api._

  def getSorted(sortBy: String = "default", direction: String = "default") = {
    val sortFun = direction match {
      case "asc" =>
        sortBy match {
          case "name" => (p: Products) => p.name.asc
          case "price" => (p: Products) => p.price.asc
          case "brand" => (p: Products) => p.brand.asc
          case _ => (p: Products) => p.price.asc
        }
      case "desc" =>
        sortBy match {
          case "name" => (p: Products) => p.name.desc
          case "price" => (p: Products) => p.price.desc
          case "brand" => (p: Products) => p.brand.desc
          case _ => (p: Products) => p.price.desc
        }
      case _ =>
        sortBy match {
          case "name" => (p: Products) => p.name.asc
          case "price" => (p: Products) => p.price.asc
          case "brand" => (p: Products) => p.brand.asc
          case _ => (p: Products) => p.price.asc
        }
    }

    products.sortBy(sortFun)
  }

  def getProductQuery(): Query[Products, ProductEntity, Seq] = products.to[Seq]

  def filtering(query: Query[Products, ProductEntity, Seq], searchString: String, column: String): Query[Products, ProductEntity, Seq] = {
    if (searchString != "" && Set("model_id", "name", "brand", "media").contains(column)) {
      query.filter { (product: Products) =>
        product.fulltextColumns(column) like s"%$searchString%"
      }
    } else {
      query
    }
  }

  def pagination(query: Query[Products, ProductEntity, Seq], request: PaginationRequest) = {
    db.run(query.paginated(request))
  }

  def search(params: Map[String, String]) = {
    val perPage = params.getOrElse("per_page", "10").toLong
    val searchString = params.getOrElse("q", "")
    val column = params.getOrElse("c", "name")
    val page = params.getOrElse("page", "1").toLong
    val sort = params.getOrElse("sort", "price")
    val direction = params.getOrElse("direction", "asc")

    val requestedPage = Page(page, perPage)
    pagination(filtering(getSorted(sort, direction), searchString, column), PaginationRequest(requestedPage))
  }
}
