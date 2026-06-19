package com.barefi0012.asesmen2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.barefi0012.asesmen2.BuildConfig
import com.barefi0012.asesmen2.auth.AuthActions
import com.barefi0012.asesmen2.data.UserPreferences
import com.barefi0012.asesmen2.model.UserProfile
import kotlinx.coroutines.launch

@Composable
fun ProfileDialog(
    userProfile: UserProfile,
    userPreferences: UserPreferences,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleApiKeyAvailable = BuildConfig.API_KEY.isNotBlank()
    var isWorking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("demo@meditrack.local") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(
                    if (userProfile.isLoggedIn) R.string.profile_title else R.string.login_title
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (userProfile.isLoggedIn) {
                    ProfileImage(userProfile)
                    Text(text = userProfile.name.ifBlank { userProfile.email })
                    Text(
                        text = userProfile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            if (googleApiKeyAvailable) R.string.login_prompt else R.string.dev_login_prompt
                        ),
                        textAlign = TextAlign.Center
                    )
                    if (!googleApiKeyAvailable) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(stringResource(R.string.label_email)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (isWorking) {
                    Spacer(modifier = Modifier.height(4.dp))
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
                message?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isWorking,
                onClick = {
                    scope.launch {
                        isWorking = true
                        message = if (userProfile.isLoggedIn) {
                            AuthActions.signOut(context, userPreferences)
                        } else if (!googleApiKeyAvailable) {
                            AuthActions.signInWithEmail(userPreferences, email)
                        } else {
                            AuthActions.signIn(context, userPreferences)
                        }
                        isWorking = false
                        if (message == null) {
                            onDismissRequest()
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(
                        when {
                            userProfile.isLoggedIn -> R.string.btn_logout
                            googleApiKeyAvailable -> R.string.btn_google_login
                            else -> R.string.btn_email_login
                        }
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.btn_close))
            }
        }
    )
}

@Composable
private fun ProfileImage(userProfile: UserProfile) {
    SubcomposeAsyncImage(
        model = userProfile.photoUrl,
        contentDescription = stringResource(
            R.string.profile_photo,
            userProfile.name.ifBlank { userProfile.email }
        ),
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        loading = {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        },
        error = {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(88.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}
