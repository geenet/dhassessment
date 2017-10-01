package com.deliveryhero.restapi.services

import com.deliveryhero.restapi.models.db.ProductEntityTable
import com.deliveryhero.restapi.models.{FetchedPage, ProductEntity, ProductEntityUpdate}
import com.deliveryhero.restapi.utils.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

class FetcherService(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext)
  extends ProductEntityTable {

  import databaseService._
  import databaseService.profile.api._

  def insertToDb(page: FetchedPage): FetchedPage = {
    db.run(products ++= page.content.map(_.toProduct))
    page
  }

  def count: Future[Int] = db.run(products.length.result)

  def getProducts(): Future[Seq[ProductEntity]] = db.run(products.result)

  def getProductById(id: String): Future[Option[ProductEntity]] =
    db.run(products.filter(_.id === id).result.headOption)

  def updateProduct(id: String, productUpdate: ProductEntityUpdate): Future[Option[ProductEntity]] = getProductById(id).flatMap {
    case Some(product) =>
      val updatedProduct = productUpdate.merge(product)
      db.run(products.filter(_.id === id).update(updatedProduct)).map(_ => Some(updatedProduct))
    case None => Future.successful(None)
  }

  def deleteProduct(id: String): Future[Int] = db.run(products.filter(_.id === id).delete)
}
