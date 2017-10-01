package com.deliveryhero.restapi.models

/**
 * Carrier class for third-party API's response
 *
 * @param content
 * @param totalElements
 * @param totalPages
 * @param page
 * @param size
 */
case class FetchedPage(
  content: List[Article],
  totalElements: Long,
  totalPages: Long,
  page: Long,
  size: Long
)
