package com.testing.ai

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testing.ai.ui.theme.Blue400
import com.testing.ai.ui.theme.GeminiAITheme
import com.testing.ai.ui.theme.Grey1
import com.testing.ai.ui.theme.Grey2

class MainActivity : ComponentActivity() {

    private val viewModel = GeminiViewModel()

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris: List<Uri>? ->
            uris?.let {
                viewModel.reset()
                it.forEach { selectedImage ->
                    uriToBitmap(selectedImage)?.let { bitmap ->
                        viewModel.addImage(bitmap)
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiAITheme {
                val chat = viewModel.chat.value
                val loading = viewModel.isLoading.value
                val unSentImages = viewModel.images
                var textState by remember { mutableStateOf(TextFieldValue("")) }

                val lazyColumnListState = rememberLazyListState()

                LaunchedEffect(chat.size) {
                    lazyColumnListState.animateScrollToItem(lazyColumnListState.layoutInfo.totalItemsCount)
                }
                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) { padding ->
                    Column(
                        Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Horizontal
                                )
                            )
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.geminiai),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .height(40.dp), contentDescription = null
                        )

                        AnimatedVisibility(
                            visible = chat.isEmpty(), modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.geminiai),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(75.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            color = Blue400,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                                Text(text = "How can I help you today?", fontSize = 20.sp)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            state = lazyColumnListState
                        ) {
                            itemsIndexed(chat) { _, message ->
                                when (message.id) {
                                    0 -> {
                                        MessageUser(message)
                                    }

                                    1 -> {
                                        MessageGemini(message)
                                    }

                                    else -> {
                                        MessageError(message)
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .background(
                                    shape = RoundedCornerShape(
                                        topStart = 10.dp,
                                        topEnd = 10.dp
                                    ), color = Grey2
                                )
                                .padding(vertical = 20.dp)
                        ) {
                            AnimatedVisibility(visible = unSentImages.isNotEmpty()) {
                                LazyRow {
                                    items(unSentImages) {
                                        Box {
                                            Image(
                                                bitmap = it.asImageBitmap(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(65.dp)
                                                    .padding(horizontal = 5.dp)
                                                    .clip(RoundedCornerShape(5.dp))
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(15.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        viewModel.removeImage(it)
                                                    }
                                                    .background(Color.Red)
                                            )
                                        }

                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth()
                            ) {

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            shape = RoundedCornerShape(200.dp),
                                            color = Grey1
                                        )
                                        .padding(16.dp)
                                ) {
                                    BasicTextField(
                                        modifier = Modifier.fillMaxWidth(),
                                        value = textState,
                                        onValueChange = {
                                            textState = it
                                        })
                                    if (textState.text.isEmpty())
                                        Text(text = "Message GeminiAI ...")

                                }
                                Spacer(modifier = Modifier.size(10.dp))
                                Button(onClick = {
                                    val pickPhotoIntent =
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    pickPhotoLauncher.launch(pickPhotoIntent)
                                }) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                }

                                Spacer(modifier = Modifier.size(10.dp))

                                if (loading) {
                                    CircularProgressIndicator()
                                } else {
                                    Button(
                                        enabled = textState.text.isNotEmpty() && unSentImages.isNotEmpty(),
                                        onClick = {
                                            val images = unSentImages.map {
                                                it
                                            }
                                            viewModel.sendPrompt(textState.text, images)
                                            textState = TextFieldValue("")
                                            viewModel.reset()

                                        }) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Enter a prompt and select images",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                            )

                        }

                    }
                }
            }
        }
    }

    @Composable
    fun MessageUser(message: GeminiViewModel.Message) {
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top, modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = message.text,
                        color = Blue400,
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topEnd = 0.dp,
                                    bottomEnd = 25.dp,
                                    bottomStart = 25.dp,
                                    topStart = 25.dp
                                )
                            )
                            .wrapContentWidth()
                            .wrapContentHeight()
                            .background(Grey1)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Row {
                        message.images.forEach {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(65.dp)
                                    .padding(5.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.size(10.dp))
                Image(
                    painterResource(id = R.drawable.profile_image),
                    contentDescription = null,
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                )

            }
        }

    }

    @Composable
    fun MessageGemini(message: GeminiViewModel.Message) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {


                Image(
                    painter = painterResource(id = R.drawable.geminiai),
                    contentDescription = null,
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = Blue400,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = message.text,
                    color = Color.White,
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topEnd = 25.dp,
                                bottomEnd = 25.dp,
                                bottomStart = 25.dp,
                                topStart = 0.dp
                            )
                        )
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .background(Blue400)
                        .padding(horizontal = 20.dp, vertical = 10.dp)

                )

            }
        }

    }


    @Composable
    fun MessageError(message: GeminiViewModel.Message) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {

                Image(
                    painter = painterResource(id = R.drawable.geminiai),
                    contentDescription = null,
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = Blue400,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = message.text,
                    color = Color.Red,
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topEnd = 25.dp,
                                bottomEnd = 25.dp,
                                bottomStart = 25.dp,
                                topStart = 0.dp
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.Red,
                            shape =  RoundedCornerShape(
                                topEnd = 25.dp,
                                bottomEnd = 25.dp,
                                bottomStart = 0.dp,
                                topStart = 25.dp
                            )
                        )
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .background(Color.Red.copy(alpha = 0.25f))
                        .padding(horizontal = 20.dp, vertical = 10.dp)

                )


            }


        }

    }


    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val contentResolver: ContentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            // Handle exception (e.g., unable to decode stream)
            null
        }
    }
}





