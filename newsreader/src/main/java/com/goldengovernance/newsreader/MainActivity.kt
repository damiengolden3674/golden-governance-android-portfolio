package com.goldengovernance.newsreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

data class Article(val id: Long, val title: String, val url: String, val author: String)
sealed interface NewsState {
    data object Loading : NewsState
    data class Success(val items: List<Article>) : NewsState
    data class Error(val message: String) : NewsState
}
fun filterArticles(items: List<Article>, query: String): List<Article> =
    items.filter { it.title.contains(query.trim(),ignoreCase = true) || it.author.contains(query.trim(),ignoreCase = true) }

@Serializable
data class StoryPayload(
 val id: Long=0L,
 val title:String?=null,
 @SerialName("by") val author:String?=null,
 val url:String?=null
)
interface HackerNewsApi {
 @GET("v0/topstories.json") suspend fun topIds(): ResponseBody
 @GET("v0/item/{id}.json") suspend fun item(@Path("id") id:Long): ResponseBody
}
class HackerNewsRepository {
 private val json=Json{ignoreUnknownKeys=true}
 private val api=Retrofit.Builder()
  .baseUrl("https://hacker-news.firebaseio.com/")
  .client(OkHttpClient.Builder().callTimeout(15,TimeUnit.SECONDS).build())
  .build().create(HackerNewsApi::class.java)
 suspend fun topStories():List<Article> = withContext(Dispatchers.IO) {
  val ids=json.decodeFromString<List<Long>>(api.topIds().string()).take(12)
  val results=ids.mapNotNull { id ->
   runCatching {
    val payload=json.decodeFromString<StoryPayload>(api.item(id).string())
    val title=payload.title?.trim().orEmpty()
    if(title.isBlank()) null else Article(id,title,payload.url ?: "https://news.ycombinator.com/item?id=${id}",payload.author ?: "unknown")
   }.getOrNull()
  }
  if(results.isEmpty()) error("No stories available; retry when connected")
  results
 }
}
class NewsViewModel : ViewModel() {
    private val repository = HackerNewsRepository()
    private val mutable = MutableStateFlow<NewsState>(NewsState.Loading)
    val uiState = mutable.asStateFlow()
    init { refresh() }
    fun refresh() = viewModelScope.launch {
        mutable.value=NewsState.Loading
        mutable.value=runCatching { NewsState.Success(repository.topStories()) }.getOrElse { NewsState.Error(it.message ?: "Network unavailable") }
    }
}
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NewsScreen() }
    }
}
@Composable fun NewsScreen(vm: NewsViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    val context=LocalContext.current
    MaterialTheme {
        Scaffold(topBar = { Text("Golden Governance • News Reader", modifier=Modifier.padding(20.dp),style=MaterialTheme.typography.titleLarge) }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(search,{search=it},label={Text("Search title or author")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                Button(onClick={vm.refresh()}) { Text("Refresh") }
                when (val s=state) {
                    NewsState.Loading -> CircularProgressIndicator()
                    is NewsState.Error -> { Text("Unable to load feed: ${s.message}"); Text("Retry or check your connection.") }
                    is NewsState.Success -> {
                        val results=filterArticles(s.items,search)
                        Text("${results.size} stories",style=MaterialTheme.typography.labelLarge)
                        LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                            items(results,key={it.id}) { story ->
                                Card(Modifier.fillMaxWidth().clickable {
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(story.url))) }
                                }) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(story.title,style=MaterialTheme.typography.titleMedium)
                                        Text("By ${story.author} • Open article",style=MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
