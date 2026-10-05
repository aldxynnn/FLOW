package com.aldxynnn.flow.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldxynnn.flow.R

/*
 * ============================================================
 * FLOW LOGIN COLORS
 * ============================================================
 */

private val FlowBackground = Color(0xFFF4F8F6)
private val FlowWhite = Color(0xFFFFFFFF)

private val FlowInk = Color(0xFF12352A)
private val FlowInkSoft = Color(0xFF3F5A50)
private val FlowMuted = Color(0xFF71817A)

private val FlowGreen = Color(0xFF087A55)
private val FlowGreenDark = Color(0xFF075B42)
private val FlowGreenSoft = Color(0xFFE6F2ED)

private val FlowBorder = Color(0xFFD8E4DE)

private val FlowError = Color(0xFFB42318)
private val FlowErrorBackground = Color(0xFFFFF1F0)


/*
 * ============================================================
 * LOGIN SCREEN
 * ============================================================
 */

@Composable
fun LoginScreen(
    loading: Boolean,
    message: String?,
    onLogin: (String, String) -> Unit,
    onDismissMessage: () -> Unit
) {
    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    val focusManager =
        LocalFocusManager.current

    val canLogin =
        username.isNotBlank() &&
                password.isNotBlank() &&
                !loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 24.dp,
                vertical = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /*
         * ========================================================
         * TOP SPACE
         * ========================================================
         */

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        /*
         * ========================================================
         * LOGO
         * ========================================================
         *
         * Tidak ada white box.
         * Logo langsung berada di atas background.
         */

        Image(
            painter = painterResource(
                id = R.drawable.flow_logo
            ),
            contentDescription = "FLOW",
            modifier = Modifier
                .size(92.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        /*
         * ========================================================
         * LOGIN FORM
         * ========================================================
         */

        LoginPanel(
            username = username,
            password = password,
            passwordVisible = passwordVisible,
            loading = loading,
            message = message,
            canLogin = canLogin,

            onUsernameChange = {
                username = it
            },

            onPasswordChange = {
                password = it
            },

            onTogglePassword = {
                passwordVisible =
                    !passwordVisible
            },

            onLogin = {
                focusManager.clearFocus()

                onLogin(
                    username.trim(),
                    password
                )
            },

            onDismissMessage = onDismissMessage,

            onPasswordDone = {
                focusManager.clearFocus()

                if (canLogin) {
                    onLogin(
                        username.trim(),
                        password
                    )
                }
            }
        )

        /*
         * ========================================================
         * BOTTOM BRAND MARK
         * ========================================================
         */

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(
                    FlowGreen.copy(
                        alpha = 0.30f
                    )
                )
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }
}


/*
 * ============================================================
 * LOGIN PANEL
 * ============================================================
 */

@Composable
private fun LoginPanel(
    username: String,
    password: String,
    passwordVisible: Boolean,
    loading: Boolean,
    message: String?,
    canLogin: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLogin: () -> Unit,
    onDismissMessage: () -> Unit,
    onPasswordDone: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(FlowWhite)
            .border(
                width = 1.dp,
                color = FlowBorder,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(
                horizontal = 20.dp,
                vertical = 22.dp
            )
    ) {

        /*
         * ========================================================
         * HEADER
         * ========================================================
         */

        Text(
            text = "Sign in",
            color = FlowInk,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.7).sp
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Access your fleet operations",
            color = FlowMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        /*
         * ========================================================
         * USERNAME
         * ========================================================
         */

        FlowFieldLabel(
            text = "USERNAME"
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        FlowInput(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = "Enter your username",

            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * ========================================================
         * PASSWORD
         * ========================================================
         */

        FlowFieldLabel(
            text = "PASSWORD"
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        FlowInput(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Enter your password",

            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            },

            trailingIcon = {
                IconButton(
                    onClick = onTogglePassword
                ) {
                    Icon(
                        imageVector =
                            if (passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },

                        contentDescription =
                            if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },

                        modifier = Modifier.size(19.dp)
                    )
                }
            },

            visualTransformation =
                if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },

            keyboardOptions = KeyboardOptions(
                keyboardType =
                    KeyboardType.Password,
                imeAction =
                    ImeAction.Done
            ),

            keyboardActions =
                KeyboardActions(
                    onDone = {
                        onPasswordDone()
                    }
                )
        )

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        /*
         * ========================================================
         * LOGIN BUTTON
         * ========================================================
         */

        Button(
            onClick = onLogin,
            enabled = canLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),

            shape = RoundedCornerShape(14.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = FlowGreen,
                contentColor = FlowWhite,

                disabledContainerColor =
                    FlowGreen.copy(
                        alpha = 0.14f
                    ),

                disabledContentColor =
                    FlowGreenDark.copy(
                        alpha = 0.45f
                    )
            ),

            elevation =
                ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 1.dp
                )
        ) {

            if (loading) {

                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = FlowWhite
                )

                Spacer(
                    modifier = Modifier.size(9.dp)
                )

                Text(
                    text = "Signing in...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

            } else {

                Text(
                    text = "Login",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            }
        }

        /*
         * ========================================================
         * ERROR MESSAGE
         * ========================================================
         */

        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {

            message?.let { error ->

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            FlowErrorBackground
                        )
                        .clickable {
                            onDismissMessage()
                        }
                        .padding(
                            horizontal = 12.dp,
                            vertical = 11.dp
                        ),

                    verticalAlignment =
                        Alignment.Top,

                    horizontalArrangement =
                        Arrangement.Start
                ) {

                    Box(
                        modifier = Modifier
                            .padding(
                                top = 5.dp
                            )
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                FlowError
                            )
                    )

                    Spacer(
                        modifier = Modifier.size(9.dp)
                    )

                    Text(
                        text = error,
                        color = FlowError,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


/*
 * ============================================================
 * FIELD LABEL
 * ============================================================
 */

@Composable
private fun FlowFieldLabel(
    text: String
) {
    Text(
        text = text,
        color = FlowInkSoft,
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.15.sp
    )
}


/*
 * ============================================================
 * FLOW INPUT
 * ============================================================
 */

@Composable
private fun FlowInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation =
        VisualTransformation.None,
    keyboardOptions: KeyboardOptions =
        KeyboardOptions.Default,
    keyboardActions: KeyboardActions =
        KeyboardActions.Default
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,

        modifier = Modifier
            .fillMaxWidth(),

        singleLine = true,

        placeholder = {
            Text(
                text = placeholder,
                color = FlowMuted.copy(
                    alpha = 0.72f
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        },

        leadingIcon = leadingIcon,

        trailingIcon = trailingIcon,

        visualTransformation =
            visualTransformation,

        keyboardOptions =
            keyboardOptions,

        keyboardActions =
            keyboardActions,

        shape = RoundedCornerShape(13.dp),

        colors =
            OutlinedTextFieldDefaults.colors(

                /*
                 * Background.
                 */
                focusedContainerColor =
                    FlowBackground,

                unfocusedContainerColor =
                    FlowBackground,

                disabledContainerColor =
                    FlowBackground,

                /*
                 * Border.
                 */
                focusedBorderColor =
                    FlowGreen,

                unfocusedBorderColor =
                    FlowBorder,

                /*
                 * Text.
                 */
                focusedTextColor =
                    FlowInk,

                unfocusedTextColor =
                    FlowInk,

                /*
                 * Leading icon.
                 */
                focusedLeadingIconColor =
                    FlowGreenDark,

                unfocusedLeadingIconColor =
                    FlowMuted,

                /*
                 * Trailing icon.
                 */
                focusedTrailingIconColor =
                    FlowGreenDark,

                unfocusedTrailingIconColor =
                    FlowMuted,

                /*
                 * Cursor.
                 */
                cursorColor =
                    FlowGreen
            )
    )
}