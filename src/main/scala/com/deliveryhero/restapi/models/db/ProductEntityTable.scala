package com.deliveryhero.restapi.models.db

import com.deliveryhero.restapi.models.ProductEntity
import com.deliveryhero.restapi.utils.DatabaseService


trait ProductEntityTable {
  protected val databaseService: DatabaseService
  import databaseService.profile.api._

  class Products(tag: Tag) extends Table[ProductEntity](tag, "products") {

    def id = column[Option[String]]("id", O.PrimaryKey)
    def modelId = column[String]("model_id")
    def name = column[String]("name")
    def brand = column[String]("brand")
    def price = column[Double]("price")
    def media = column[String]("media")


    def * = (id,
      modelId,
      name,
      brand,
      price,
      media
    ) <> ((ProductEntity.apply _).tupled, ProductEntity.unapply)

    def fulltextColumns = Map(
      "modelId" -> modelId,
      "name" -> name,
      "brand" -> brand,
      "media" -> media
    )
  }

  protected val products = TableQuery[Products]
}
