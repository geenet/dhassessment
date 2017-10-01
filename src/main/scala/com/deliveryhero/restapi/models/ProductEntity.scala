package com.deliveryhero.restapi.models

/**
 * ProductEntity which will be used for database access.
 *
 * @param id
 * @param modelId
 * @param name
 * @param brand
 * @param price
 * @param media
 */
case class ProductEntity(
  id: Option[String] = None,
  modelId: String,
  name: String,
  brand: String,
  price: Double,
  media: String
)

case class ProductEntityUpdate(
  id: Option[String] = None,
  modelId: Option[String],
  name: Option[String],
  brand: Option[String],
  price: Option[Double],
  media: Option[String]) {
  def merge(product: ProductEntity): ProductEntity = {
    ProductEntity(
      id = product.id,
      modelId = modelId.getOrElse(product.modelId),
      name = name.getOrElse(product.name),
      brand = brand.getOrElse(product.brand),
      price = price.getOrElse(product.price),
      media = media.getOrElse(product.media)
    )
  }
}

