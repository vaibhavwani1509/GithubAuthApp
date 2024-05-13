package com.vaibhavwani.githubapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vaibhavwani.githubapp.data.model.GithubComment
import com.vaibhavwani.githubapp.data.model.GithubPR
import com.vaibhavwani.githubapp.data.model.GithubRepo
import com.vaibhavwani.githubapp.ui.Spinner
import com.vaibhavwani.githubapp.ui.theme.GithubAppTheme
import com.vaibhavwani.githubapp.viewmodel.GithubViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val viewmodel by viewModels<GithubViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.tokenFlow.collect {
                    it?.let {
                        if (it.isNotEmpty()) {
                            viewmodel.token = it
                            Toast.makeText(this@MainActivity, "Token Success", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
        setContent {
            GithubAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewmodel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val uri = intent.data
        if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
            val code = uri.getQueryParameter("code")
            Log.d("VAIBHAV", "MainActivity.kt :onResume: 42 code = " + code)
            code?.let {
                viewmodel.getToken(
                    code = it,
                    clientId = CLIENT_ID,
                    clientSecret = CLIENT_SECRET,
                )
            }
        }
    }
}

@Composable
fun MainScreen(viewmodel: GithubViewModel) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }
    val scope = rememberCoroutineScope()
    var repos by remember { mutableStateOf<List<GithubRepo>?>(null) }
    var prs by remember { mutableStateOf<List<GithubPR>?>(null) }
    var comments by remember { mutableStateOf<List<GithubComment>?>(null) }

    LaunchedEffect(key1 = Unit) {
        scope.launch {
            viewmodel.repoFlow.collect {
                repos = it
            }
        }
        scope.launch {
            viewmodel.prFlow.collect {
                prs = it
            }
        }

        scope.launch {
            viewmodel.commentFlow.collect {
                comments = it
            }
        }
    }

    val postComment = viewmodel.postCommentFlow.collectAsState(false)
    if (postComment.value) {
        Toast.makeText(LocalContext.current, "Comment posted successfully", Toast.LENGTH_SHORT).show()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "${OAUTH_URL}?client_id=${CLIENT_ID}&scope=repo&redirect_uri=${CALLBACK_URL}"
                )
            )
            launcher.launch(intent)
        }) {
            Text(text = "Authenticate")
        }

        Spacer(modifier = Modifier.height(20.dp))

        val sectionModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
            .padding(top = 15.dp)
            .background(
                color = Color.Cyan.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(20.dp)
        val isAuthenticated = viewmodel.tokenFlow.collectAsState().value?.isNotEmpty() ?: false

        // REPOs
        Row(sectionModifier) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(modifier = Modifier.padding(horizontal = 8.dp), text = "Github Repos", style = TextStyle(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                CustomSpinner(
                    items = repos,
                    preSelectedItem = repos?.getOrNull(0),
                    getTextToShow = { it.name ?: "" }
                ) {
                    // On Repo selected, fetch PRs
                    viewmodel.getAllPRs(
                        owner = it.owner?.login ?: "",
                        repo = it.name ?: "",
                    )
                }
            }
            Button(
                enabled = isAuthenticated,
                onClick = {
                    viewmodel.getAllRepos()
                }) {
                Text(text = "Get All repos")
            }
        }

        // PRs
        Spacer(modifier = Modifier.height(20.dp))
        Row(sectionModifier) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(modifier = Modifier.padding(horizontal = 8.dp), text = "Github PRs", style = TextStyle(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                CustomSpinner(
                    items = prs,
                    preSelectedItem = prs?.getOrNull(0),
                    getTextToShow = { it.title ?: "" }
                ){
                    // On PR selected, fetch comments
                    viewmodel.getComments(
                        pr = it.number ?: ""
                    )
                }
            }
        }

        // COMMENTs
        Spacer(modifier = Modifier.height(20.dp))
        Row(sectionModifier) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(modifier = Modifier.padding(horizontal = 8.dp), text = "Github COMMENTs", style = TextStyle(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                CustomSpinner(
                    items = comments,
                    preSelectedItem = comments?.getOrNull(0),
                    getTextToShow = { it.body ?: "" }
                )
            }
        }

        var comment by remember { mutableStateOf<String>("Enter a comment") }
        Spacer(modifier = Modifier.height(20.dp))
        Row(sectionModifier) {
            TextField(
                modifier = Modifier.weight(1f),
                value = comment,
                onValueChange = {
                comment = it
            }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = {
                viewmodel.postComment(GithubComment(null, comment))
            }) {
                Text(text = "Post Comment")
            }
        }
    }
}

@Composable
fun <T> CustomSpinner(
    items: List<T>?,
    getTextToShow: (T) -> String,
    preSelectedItem: T? = null,
    onItemSelected: ((T) -> Unit)? = null
) {
    var selectedItem by remember { mutableStateOf(preSelectedItem) }
    Spinner(
        modifier = Modifier.wrapContentSize(),
        dropDownModifier = Modifier.wrapContentSize(),
        items = items ?: emptyList(),
        selectedItem = selectedItem,
        onItemSelected = {
            selectedItem = it
            onItemSelected?.invoke(it)
        },
        selectedItemFactory = { modifier, item ->
            Row(
                modifier = modifier
                    .wrapContentSize()
                    .padding(8.dp)
            ) {
                Text(getTextToShow(item))
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
        },
        dropdownItemFactory = { item, _ ->
            Text(getTextToShow(item))
        }
    )
}
