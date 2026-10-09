package com.goldengovernance.newsreader
import org.junit.Assert.assertEquals
import org.junit.Test
class ArticleFilterTest {
 @Test fun filterByTitleAndAuthor() {
   val items=listOf(Article(1,"Android release","https://example.com/a","Alice"),Article(2,"SwiftUI news","https://example.com/b","Bob"))
   assertEquals(1,filterArticles(items,"SWIFT").size)
   assertEquals(1,filterArticles(items,"alice").size)
 }
 @Test fun emptyQueryReturnsAll() {
   val items=listOf(Article(1,"One","https://example.com","writer"))
   assertEquals(items,filterArticles(items,""))
 }
}
