package com.prestongarno.apis

import com.prestongarno.apis.core.entities.Api


interface SearchService {

  fun getApiById(id: Int): Api?

  fun getApiById(ids: Iterable<Int>): Iterable<Api>

  fun fuzzySearch(text: String): Iterable<Api>
}
