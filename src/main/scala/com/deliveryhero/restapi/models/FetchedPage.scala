package com.deliveryhero.restapi.models

case class FetchedPage(
  content: List[Article],
  totalElements: Long,
  totalPages: Long,
  page: Long,
  size: Long
)
