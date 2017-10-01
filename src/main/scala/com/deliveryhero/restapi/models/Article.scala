package com.deliveryhero.restapi.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Article case class for parsing fields for Json deserializer.
 *
 * @param id
 * @param modelId
 * @param name
 * @param brand
 * @param units
 * @param media
 */
case class Article(
  id: Option[String] = None,
  modelId: String,
  name: String,
  brand: Brand,
  units: List[Units],
  media: Media
) {
  def toProduct = ProductEntity(
    id = this.id,
    modelId = this.modelId,
    name = this.name,
    brand = this.brand.name,
    price = this.units.head.price.value,
    media = this.media.images.head.smallUrl
  )
}

case class Brand(
  name: String
)

case class Price(
  value: Double
)

case class Units(
  price: Price
)

case class Image(
  smallUrl: String
)

case class Media(
  images: List[Image]
)

trait ArticlesParserProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val priceFormat = jsonFormat1(Price)
  implicit val brandFormat = jsonFormat1(Brand)
  implicit val unitsFormat = jsonFormat1(Units)
  implicit val imageFormat = jsonFormat1(Image)
  implicit val mediaFormat = jsonFormat1(Media)

  implicit val articlesFormat = jsonFormat6(Article)
}
