package com.lydia.medrem.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lydia.medrem.R
import com.lydia.medrem.ui.screens.auth.components.AuthButton
import com.lydia.medrem.ui.screens.auth.components.AuthTextField
import com.lydia.medrem.ui.screens.auth.components.TextDivider
import com.lydia.medrem.ui.utils.UiEvent

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun AuthScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    onPopBackStack: (UiEvent.PopBackStack) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                is UiEvent.PopBackStack -> onPopBackStack(event)
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(
                            id = if (viewModel.isLoginMode) R.string.login else R.string.sign_up
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.onEvent(AuthEvents.OnNavigationIconClick)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App logo
                Image(
                    painter = painterResource(id = R.drawable.pill_rem),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(16.dp)
                )
                
                // Welcome text
                Text(
                    text = stringResource(
                        id = if (viewModel.isLoginMode) R.string.welcome_back else R.string.create_account
                    ),
                    style = MaterialTheme.typography.h5,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Description
                Text(
                    text = stringResource(
                        id = if (viewModel.isLoginMode) R.string.login_description else R.string.signup_description
                    ),
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Error message
                AnimatedVisibility(
                    visible = viewModel.errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    viewModel.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Auth fields
                AnimatedVisibility(
                    visible = !viewModel.isLoginMode,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    AuthTextField(
                        value = viewModel.username,
                        onValueChange = { viewModel.onEvent(AuthEvents.OnUsernameChange(it)) },
                        label = stringResource(id = R.string.username),
                        leadingIcon = Icons.Default.Person,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }
                
                AuthTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.onEvent(AuthEvents.OnEmailChange(it)) },
                    label = stringResource(id = R.string.email),
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                AuthTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.onEvent(AuthEvents.OnPasswordChange(it)) },
                    label = stringResource(id = R.string.password),
                    leadingIcon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    keyboardActions = KeyboardActions(
                        onNext = { 
                            if (viewModel.isLoginMode) {
                                keyboardController?.hide()
                                viewModel.onEvent(AuthEvents.OnLoginClick)
                            } else {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        },
                        onDone = {
                            keyboardController?.hide()
                            if (viewModel.isLoginMode) {
                                viewModel.onEvent(AuthEvents.OnLoginClick)
                            }
                        }
                    ),
                    imeAction = if (viewModel.isLoginMode) ImeAction.Done else ImeAction.Next,
                    isPassword = true
                )
                
                AnimatedVisibility(
                    visible = !viewModel.isLoginMode,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    AuthTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = { viewModel.onEvent(AuthEvents.OnConfirmPasswordChange(it)) },
                        label = stringResource(id = R.string.confirm_password),
                        leadingIcon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                viewModel.onEvent(AuthEvents.OnSignUpClick)
                            }
                        ),
                        imeAction = ImeAction.Done,
                        isPassword = true
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AuthButton(
                    text = stringResource(id = if (viewModel.isLoginMode) R.string.login else R.string.sign_up),
                    onClick = {
                        if (viewModel.isLoginMode) {
                            viewModel.onEvent(AuthEvents.OnLoginClick)
                        } else {
                            viewModel.onEvent(AuthEvents.OnSignUpClick)
                        }
                    },
                    enabled = if (viewModel.isLoginMode) {
                        viewModel.email.isNotEmpty() && viewModel.password.isNotEmpty()
                    } else {
                        viewModel.email.isNotEmpty() && 
                        viewModel.password.isNotEmpty() &&
                        viewModel.confirmPassword.isNotEmpty() &&
                        viewModel.username.isNotEmpty() &&
                        viewModel.password == viewModel.confirmPassword
                    },
                    isLoading = viewModel.isLoading
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextDivider(text = stringResource(id = R.string.or))
                
                TextButton(
                    onClick = {
                        if (viewModel.isLoginMode) {
                            viewModel.onEvent(AuthEvents.OnNavigateToSignUp)
                        } else {
                            viewModel.onEvent(AuthEvents.OnNavigateToLogin)
                        }
                    },
                    modifier = Modifier.padding(8.dp),
                    enabled = !viewModel.isLoading
                ) {
                    Text(
                        text = stringResource(
                            id = if (viewModel.isLoginMode) R.string.dont_have_account else R.string.already_have_account
                        ),
                        color = if (!viewModel.isLoading) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.5f)
                    )
                }
                
                // Add some padding at the bottom
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}